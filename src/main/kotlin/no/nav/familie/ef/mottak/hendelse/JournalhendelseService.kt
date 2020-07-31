package no.nav.familie.ef.mottak.hendelse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.task.LagJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.log.IdUtils
import no.nav.familie.log.mdc.MDCConstants
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class JournalhendelseService(val journalpostClient: IntegrasjonerClient,
                             val taskRepository: TaskRepository,
                             val hendelsesloggRepository: HendelsesloggRepository,
                             val featureToggleService: FeatureToggleService) {

    val kanalNavnoCounter: Counter = Metrics.counter("barnetrygd.journalhendelse.kanal.navno")
    val kanalSkannetsCounter: Counter = Metrics.counter("barnetrygd.journalhendelse.kanal.skannets")
    val kanalAnnetCounter: Counter = Metrics.counter("barnetrygd.journalhendelse.kanal.annet")
    val ignorerteCounter: Counter = Metrics.counter("barnetrygd.journalhendelse.ignorerte")
    val feilCounter: Counter = Metrics.counter("barnetrygd.journalhendelse.feilet")
    val logger: Logger = LoggerFactory.getLogger(JournalhendelseService::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    fun prosesserNyHendelse(consumerRecord: ConsumerRecord<Long, JournalfoeringHendelseRecord>,
                            ack: Acknowledgment) {
        try {
            val hendelseRecord = consumerRecord.value()
            val callId = hendelseRecord.getKanalReferanseId().toStringOrNull() ?: IdUtils.generateId()
            MDC.put(MDCConstants.MDC_CALL_ID, callId)

            if (hendelsesloggRepository.existsByHendelseId(hendelseRecord.getHendelsesId().toString())) {
                ack.acknowledge()
                return
            }

            if (erGyldigHendelsetype(hendelseRecord)) {
                secureLogger.info("Mottatt gyldig hendelse: $hendelseRecord")
                behandleJournalhendelse(hendelseRecord)
            }

            hendelsesloggRepository
                    .save(Hendelseslogg(consumerRecord.offset(),
                                        hendelseRecord.getHendelsesId().toString(),
                                        mapOf("journalpostId" to hendelseRecord.getJournalpostId().toString(),
                                              "hendelsesType" to hendelseRecord.getHendelsesType().toString()).toProperties()))
            ack.acknowledge()
        } catch (e: Exception) {
            logger.error("Feil ved lesing av journalhendelser ", e)
            feilCounter.count()
            throw e
        } finally {
            MDC.clear()
        }
    }

    fun CharSequence.toStringOrNull(): String? {
        return if (!this.isBlank()) this.toString() else null
    }


    private fun erGyldigHendelsetype(hendelseRecord: JournalfoeringHendelseRecord): Boolean {
        return GYLDIGE_HENDELSE_TYPER.contains(hendelseRecord.getHendelsesType().toString())
               && (hendelseRecord.getTemaNytt() != null && hendelseRecord.getTemaNytt().toString() == "BAR")
    }

    fun behandleJournalhendelse(hendelseRecord: JournalfoeringHendelseRecord) {
        //hent journalpost fra saf
        val journalpostId = hendelseRecord.getJournalpostId().toString()
        val journalpost = journalpostClient.hentJournalpost(journalpostId)
        if (skalBehandleJournalpost(journalpost)) {

            if (journalpost.journalstatus == Journalstatus.MOTTATT) {
                when {
                    "SKAN_" == journalpost.kanal?.substring(0, 5) -> {
                        behandleSkanningHendelser(journalpost)
                    }
                    "NAV_NO" == journalpost.kanal -> {
                        behandleNavnoHendelser(journalpost)
                    }
                    else -> {
                        logger.info("Ny journalhendelse med journalpostId=$journalpostId med status MOTTATT " +
                                    "og kanal ${journalpost.kanal}")
                        kanalAnnetCounter.count()
                    }
                }
            } else {
                logger.debug("Ignorer journalhendelse hvor journalpost=$journalpostId har status ${journalpost.journalstatus}")
                ignorerteCounter.count()
            }
        }
    }

    private fun behandleNavnoHendelser(journalpost: Journalpost) {
        if (featureToggleService.isEnabled("familie-ef-mottak.journalhendelse.behsak")) {
            // TODO Sjekk om det finnes sak på brukeren før (Gsak-sak i Joark).
            // Sak finnes LagJournalføringsoppgaveTask
            // sak finne ikke ny linjesak i Infotrygd-task

            logger.info("Oppretter task OppdaterOgFerdigstillJournalpostTask, feature skrudd på")
        } else {
            logger.info("Behandler ikke journalhendelse, feature familie-ef-mottak.journalhendelse.behsak er skrudd av i Unleash")
        }

        kanalNavnoCounter.increment()
    }

    private fun behandleSkanningHendelser(journalpost: Journalpost) {
        logger.info("Ny Journalhendelse med [journalpostId=${journalpost.journalpostId}, " +
                    "status=${journalpost.journalstatus}, " +
                    "tema=${journalpost.tema}, " +
                    "kanal=${journalpost.kanal}]")

        if (featureToggleService.isEnabled("familie-ef-mottak.journalhendelse.jfr")) {
            val metadata = opprettMetadata(journalpost)
            val journalføringsTask = Task.nyTask(LagJournalføringsoppgaveTask.TYPE,
                                                 journalpost.journalpostId,
                                                 metadata)
            taskRepository.save(journalføringsTask)
        } else {
            logger.info("Behandler ikke journalhendelse, feature familie-ef-mottak.journalhendelse.jfr er skrudd av i Unleash")
        }

        kanalSkannetsCounter.increment()
    }

    private fun skalBehandleJournalpost(journalpost: Journalpost) =
            journalpost.tema == "BAR" && journalpost.journalposttype == Journalposttype.I


    private fun opprettMetadata(journalpost: Journalpost): Properties {
        return Properties().apply {
            if (journalpost.bruker != null) {
                this["personIdent"] = journalpost.bruker!!.id
            }
            this["journalpostId"] = journalpost.journalpostId
            if (!MDC.get(MDCConstants.MDC_CALL_ID).isNullOrEmpty()) {
                this["callId"] = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
            }
        }
    }

    companion object {
        private val GYLDIGE_HENDELSE_TYPER = arrayOf("MidlertidigJournalført", "TemaEndret")
    }
}

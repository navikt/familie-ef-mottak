package no.nav.familie.ef.mottak.hendelse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.task.LagEksternJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.log.IdUtils
import no.nav.familie.log.mdc.MDCConstants
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

/**
 * TODO : Trekk ut alt IA relatert (f.eks Prometheus-ting) fra serviceklassen. Evt legg til egne unntakstyper.
 */
@Service
class JournalhendelseService(val journalpostClient: IntegrasjonerClient,
                             val taskRepository: TaskRepository,
                             val hendelseloggRepository: HendelsesloggRepository,
                             val featureToggleService: FeatureToggleService,
                             val soknadRepository: SoknadRepository) {

    val kanalNavnoCounter: Counter = Metrics.counter("alene.med.barn.journalhendelse.kanal.navno")
    val kanalSkannetsCounter: Counter = Metrics.counter("alene.med.barn.journalhendelse.kanal.skannets")
    val kanalAnnetCounter: Counter = Metrics.counter("alene.med.barn.journalhendelse.kanal.annet")
    val ignorerteCounter: Counter = Metrics.counter("alene.med.barn.journalhendelse.ignorerte")
    val logger: Logger = LoggerFactory.getLogger(JournalhendelseService::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @Transactional
    fun prosesserNyHendelse(
            hendelseRecord: JournalfoeringHendelseRecord,
            offset: Long) {

        if (hendelseRegistrertIHendelseslogg(hendelseRecord)) {
            return
        }

        if (hendelseRecord.skalProsessereHendelse()) {
            secureLogger.info("Mottatt gyldig hendelse: $hendelseRecord")
            behandleJournalhendelse(hendelseRecord)
        }
        lagreHendelseslogg(hendelseRecord, offset)
    }

    fun JournalfoeringHendelseRecord.skalProsessereHendelse() = erRiktigTemaNytt(this)
                                                                && erGyldigHendelsetype(this)

    fun hendelseRegistrertIHendelseslogg(hendelseRecord: JournalfoeringHendelseRecord) =
            hendelseloggRepository.existsByHendelseId(hendelseRecord.hendelsesId.toString())

    // TODO Får vi en kopi av alle hendeler
    @Transactional
    fun lagreHendelseslogg(hendelseRecord: JournalfoeringHendelseRecord,
                           offset: Long) {
        hendelseloggRepository
                .save(Hendelseslogg(offset,
                                    hendelseRecord.hendelsesId.toString(),
                                    mapOf("journalpostId" to hendelseRecord.journalpostId.toString(),
                                          "hendelsesType" to hendelseRecord.hendelsesType.toString()).toProperties()))
    }


    private fun erGyldigHendelsetype(hendelseRecord: JournalfoeringHendelseRecord): Boolean {
        return GYLDIGE_HENDELSE_TYPER.contains(hendelseRecord.hendelsesType.toString())
    }

    private fun erRiktigTemaNytt(hendelseRecord: JournalfoeringHendelseRecord) =
            (hendelseRecord.temaNytt != null && hendelseRecord.temaNytt.toString() == "ENF")

    fun behandleJournalhendelse(hendelseRecord: JournalfoeringHendelseRecord) {
        //hent journalpost fra saf
        val journalpostId = hendelseRecord.journalpostId.toString()
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
                        logger.error("Ny journalhendelse med journalpostId=$journalpostId med status MOTTATT " +
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

        if (featureToggleService.isEnabled("familie.ef.mottak.journalhendelse-behsak")) {
            when (val søknad = soknadRepository.findByJournalpostId(journalpost.journalpostId)) {
                null -> lagEksternJournalføringsTask(journalpost)
                else -> logger.info("Hendelse mottatt for digital søknad ${søknad.id}")
            }
        } else {
            logger.info("Behandler ikke journalhendelse, feature familie.ef.mottak.journalhendelse-behsak er skrudd av i Unleash")
        }

        kanalNavnoCounter.increment()
    }

    private fun behandleSkanningHendelser(journalpost: Journalpost) {
        logger.info("Ny Journalhendelse med [journalpostId=${journalpost.journalpostId}, " +
                    "status=${journalpost.journalstatus}, " +
                    "tema=${journalpost.tema}, " +
                    "kanal=${journalpost.kanal}]")

        if (featureToggleService.isEnabled("familie.ef.mottak.journalhendelse-jfr-skannede")) {
            lagEksternJournalføringsTask(journalpost)
        } else {
            logger.info("Behandler ikke journalhendelse, feature familie.ef.mottak.journalhendelse-jfr-skannede er skrudd av i Unleash")
        }

        kanalSkannetsCounter.increment()
    }

    private fun lagEksternJournalføringsTask(journalpost: Journalpost) {
        logger.info("Oppretter task LagEksternJournalføringsoppgaveTask, feature skrudd på")
        val metadata = opprettMetadata(journalpost)
        val journalføringsTask = Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                      payload = journalpost.journalpostId,
                                      metadata = metadata)
        taskRepository.save(journalføringsTask)
    }

    private fun skalBehandleJournalpost(journalpost: Journalpost) =
            journalpost.tema == "ENF" && journalpost.journalposttype == Journalposttype.I


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

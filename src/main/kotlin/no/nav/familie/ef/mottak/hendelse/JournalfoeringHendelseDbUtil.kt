package no.nav.familie.ef.mottak.hendelse

import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.task.LagEksternJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.log.IdUtils
import no.nav.familie.log.mdc.MDCConstants
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import java.util.Properties
import javax.transaction.Transactional

@Service
class JournalfoeringHendelseDbUtil(val hendelseloggRepository: HendelsesloggRepository,
                                   val taskRepository: TaskRepository) {

    val logger: Logger = LoggerFactory.getLogger(JournalfoeringHendelseDbUtil::class.java)

    @Transactional
    fun lagreHendelseslogg(hendelseRecord: JournalfoeringHendelseRecord, offset: Long): Hendelseslogg {
        return hendelseloggRepository
                .save(Hendelseslogg(offset,
                                    hendelseRecord.hendelsesId.toString(),
                                    mapOf("journalpostId" to hendelseRecord.journalpostId.toString(),
                                          "hendelsesType" to hendelseRecord.hendelsesType.toString()).toProperties()))
    }

    fun erHendelseRegistrertIHendelseslogg(hendelseRecord: JournalfoeringHendelseRecord) =
            hendelseloggRepository.existsByHendelseId(hendelseRecord.hendelsesId.toString())

    fun lagreEksternJournalføringsTask(journalpost: Journalpost) {
        logger.info("Oppretter task LagEksternJournalføringsoppgaveTask, feature skrudd på")
        val metadata = opprettMetadata(journalpost)
        val journalføringsTask = Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                      payload = journalpost.journalpostId,
                                      metadata = metadata)
        taskRepository.save(journalføringsTask)
    }

    private fun opprettMetadata(journalpost: Journalpost): Properties {
        return Properties().apply {
            this["personIdent"] = journalpost.bruker?.id
            this["journalpostId"] = journalpost.journalpostId
            if (!MDC.get(MDCConstants.MDC_CALL_ID).isNullOrEmpty()) {
                this["callId"] = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
            }
        }
    }
}
package no.nav.familie.ef.mottak.hendelse

import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.task.LagEksternJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.prosessering.domene.Task
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class JournalfoeringHendelseDbUtil(val hendelseloggRepository: HendelsesloggRepository,
                                   val taskRepository: TaskRepositoryUtvidet) {

    val logger: Logger = LoggerFactory.getLogger(JournalfoeringHendelseDbUtil::class.java)

    @Transactional
    fun lagreHendelseslogg(hendelseRecord: JournalfoeringHendelseRecord, offset: Long): Hendelseslogg {
        return hendelseloggRepository
                .save(Hendelseslogg(offset,
                                    hendelseRecord.hendelsesId.toString(),
                                    mapOf("journalpostId" to hendelseRecord.journalpostId.toString(),
                                          "hendelsesType" to hendelseRecord.hendelsesType.toString()).toProperties()))
    }

    fun erHendelseRegistrertIHendelseslogg(hendelsesId: String) =
            hendelseloggRepository.existsByHendelseId(hendelsesId)

    fun harIkkeOpprettetOppgaveForJournalpost(hendelseRecord: JournalfoeringHendelseRecord): Boolean {
        return !taskRepository.existsByPayloadAndType(hendelseRecord.journalpostId.toString(),
                                                      LagEksternJournalføringsoppgaveTask.TYPE)
    }

    fun lagreEksternJournalføringsTask(journalpost: Journalpost) {
        val journalføringsTask = Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                      payload = journalpost.journalpostId,
                                      metadata = journalpost.metadata())
        taskRepository.save(journalføringsTask)
    }

}
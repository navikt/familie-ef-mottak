package no.nav.familie.ef.mottak.hendelse

import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.task.LagEksternJournalføringsoppgaveTask
import no.nav.familie.ef.mottak.task.eksternJournalføringFlyt
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.prosessering.domene.PropertiesWrapper
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class JournalfoeringHendelseDbUtil(
    val hendelseloggRepository: HendelsesloggRepository,
    val taskService: TaskService,
    val taskRepositoryUtvidet: TaskRepositoryUtvidet,
) {
    val logger: Logger = LoggerFactory.getLogger(JournalfoeringHendelseDbUtil::class.java)

    @Transactional
    fun lagreHendelseslogg(
        hendelseRecord: JournalfoeringHendelseRecord,
        offset: Long,
    ): Hendelseslogg =
        hendelseloggRepository
            .insert(
                Hendelseslogg(
                    offset,
                    hendelseRecord.hendelsesId.toString(),
                    PropertiesWrapper(
                        mapOf(
                            "journalpostId" to hendelseRecord.journalpostId.toString(),
                            "hendelsesType" to hendelseRecord.hendelsesType.toString(),
                        ).toProperties(),
                    ),
                ),
            )

    fun erHendelseRegistrertIHendelseslogg(hendelsesId: String) = hendelseloggRepository.existsByHendelseId(hendelsesId)

    fun harIkkeOpprettetOppgaveForJournalpost(hendelseRecord: JournalfoeringHendelseRecord): Boolean =
        !taskRepositoryUtvidet.existsByPayloadAndType(
            hendelseRecord.journalpostId.toString(),
            LagEksternJournalføringsoppgaveTask.TYPE,
        )

    fun lagreEksternJournalføringsTask(journalpost: Journalpost) {
        val journalføringsTask =
            Task(
                type = eksternJournalføringFlyt().first().type,
                payload = journalpost.journalpostId,
                properties = journalpost.metadata(),
            ).medTriggerTid(LocalDateTime.now().plusMinutes(15))
        taskService.save(journalføringsTask)
    }
}

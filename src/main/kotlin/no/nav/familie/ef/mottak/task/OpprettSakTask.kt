package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = OpprettSakTask.TYPE,
                     beskrivelse = "Oppretter sak i Infotrygd")
class OpprettSakTask(private val taskRepository: TaskRepository,
                     private val sakService: SakService,
                     private val soknadRepository: SoknadRepository) : AsyncTaskStep {


    override fun doTask(task: Task) {
        val sakId = sakService.opprettSakOmIngenFinnes(task.payload)

        if (sakId != null) {
            val soknad = soknadRepository.findByIdOrNull(task.payload) ?: error("Søknad har forsvunnet!")
            val soknadMedSaksnummer = soknad.copy(saksnummer = sakId)
            soknadRepository.save(soknadMedSaksnummer)
        }

    }

    override fun onCompletion(task: Task) {
        val soknad = soknadRepository.findByIdOrNull(task.payload) ?: error("Søknad har forsvunnet!")

        val nesteTask = if (soknad.saksnummer == null) {
            Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        } else {
            // Ferdigstill journalføringTask
            Task(FerdigstillJournalføringTask.TYPE, task.payload, task.metadata)
        }

        taskRepository.save(nesteTask)
    }

    companion object {

        const val TYPE = "opprettSak"
    }
}

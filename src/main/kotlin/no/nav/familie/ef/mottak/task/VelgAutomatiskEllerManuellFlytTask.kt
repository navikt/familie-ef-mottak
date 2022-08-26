package no.nav.familie.ef.mottak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = VelgAutomatiskEllerManuellFlytTask.TYPE,
    beskrivelse = "Velg automatisk eller manuel flyt"
)
class VelgAutomatiskEllerManuellFlytTask(val taskRepository: TaskRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {

        val neste = if (true) {
            manuellJournalføringFlyt().first()
        } else {
            automatiskJournalføringFlyt().first()
        }

        val nesteTask = Task(neste.type, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {
        const val TYPE = "VelgAutomatiskEllerManuelFlyt"
    }
}

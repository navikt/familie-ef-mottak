package no.nav.familie.ef.mottak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = AutomatiskJournalførTask.TYPE, beskrivelse = "Automatisk journalfør")
class AutomatiskJournalførTask : AsyncTaskStep {

    override fun doTask(task: Task) {
        TODO("Not yet implemented")
    }

    companion object {
        const val TYPE = "automatiskJournalfør"
    }
}

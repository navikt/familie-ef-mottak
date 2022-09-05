package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.AutomatiskJournalføringService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = AutomatiskJournalførTask.TYPE, beskrivelse = "Automatisk journalfør")
class AutomatiskJournalførTask(
    val søknadService: SøknadService,
    val taskRepository: TaskRepository,
    val automatiskJournalføringService: AutomatiskJournalføringService
) :
    AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknad: Søknad = søknadService.get(task.payload)
        val stønadstype: StønadType =
            dokumenttypeTilStønadType(søknad.dokumenttype) ?: error("Må ha stønadstype for å automatisk journalføre")
        val journalpostId = task.metadata["journalpostId"].toString()

        val automatiskJournalføringFullført =
            automatiskJournalføringService.journalførAutomatisk(
                personIdent = søknad.fnr,
                journalpostId = journalpostId,
                stønadstype = stønadstype
            )

        if (!automatiskJournalføringFullført) {
            val nesteFallbackTaskType = manuellJournalføringFlyt().first().type
            val fallback = Task(nesteFallbackTaskType, task.payload, task.metadata)
            taskRepository.save(fallback)
        }
    }

    companion object {
        const val TYPE = "automatiskJournalfør"
    }
}

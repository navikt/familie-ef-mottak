package no.nav.familie.ef.mottak.task

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics.counter
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.AutomatiskJournalføringService
import no.nav.familie.ef.mottak.service.MappeService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = AutomatiskJournalførTask.TYPE, beskrivelse = "Automatisk journalfør")
class AutomatiskJournalførTask(
    val søknadService: SøknadService,
    val taskService: TaskService,
    val automatiskJournalføringService: AutomatiskJournalføringService,
    val integrasjonerClient: IntegrasjonerClient,
    val mappeService: MappeService,
) :
    AsyncTaskStep {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val antallAutomatiskJournalført: Counter = counter("alene.med.barn.automatiskjournalfort")

    override fun doTask(task: Task) {
        val søknad: Søknad = søknadService.get(task.payload)
        val stønadstype: StønadType =
            dokumenttypeTilStønadType(søknad.dokumenttype) ?: error("Må ha stønadstype for å automatisk journalføre")
        val journalpostId = task.metadata["journalpostId"].toString()

        val enhet = integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(søknad.fnr).firstOrNull()?.enhetId
        val mappeId = mappeService.finnMappeIdForSøknadOgEnhet(søknad.id, enhet)
        val automatiskJournalføringFullført =
            automatiskJournalføringService.journalførAutomatisk(
                personIdent = søknad.fnr,
                journalpostId = journalpostId,
                stønadstype = stønadstype,
                mappeId = mappeId,
                søknad = søknad,
            )

        when (automatiskJournalføringFullført) {
            true -> antallAutomatiskJournalført.increment()
            false -> brukManuellJournalføring(journalpostId, task)
        }
    }

    private fun brukManuellJournalføring(
        journalpostId: String,
        task: Task,
    ) {
        logger.warn("Kunne ikke automatisk journalføre $journalpostId - fortsetter derfor på manuell journalføringsflyt")
        val nesteFallbackTaskType = manuellJournalføringFlyt().first().type
        val fallback = Task(nesteFallbackTaskType, task.payload, task.metadata)
        taskService.save(fallback)
    }

    companion object {
        const val TYPE = "automatiskJournalfør"
    }
}

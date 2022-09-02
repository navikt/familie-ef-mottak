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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@TaskStepBeskrivelse(taskStepType = AutomatiskJournalførTask.TYPE, beskrivelse = "Automatisk journalfør")
class AutomatiskJournalførTask(
    val søknadService: SøknadService,
    val taskRepository: TaskRepository,
    val automatiskJournalføringService: AutomatiskJournalføringService
) :
    AsyncTaskStep {

    @Transactional
    override fun doTask(task: Task) {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
        val søknad: Søknad = søknadService.get(task.payload)
        val stønadstype: StønadType =
            dokumenttypeTilStønadType(søknad.dokumenttype) ?: error("Må ha stønadstype for å automatisk journalføre")
        val journalpostId = task.metadata["journalpostId"].toString()

        try {
            val response = automatiskJournalføringService.lagFørstegangsbehandlingOgBehandleSakOppgave(
                personIdent = søknad.fnr,
                journalpostId = journalpostId,
                stønadstype = stønadstype
            )
            logger.info(
                "Automatisk journalført:$journalpostId: " +
                    "behandlingId: ${response.behandlingId}, " +
                    "fagsakId: ${response.fagsakId}, " +
                    "behandleSakOppgaveId: ${response.behandleSakOppgaveId}"
            )
        } catch (e: Exception) {
            logger.warn("Feil ved prosessering av automatisk journalhendelser for $stønadstype: journalpostId: $journalpostId, fallback => manuell")
            val nesteFallbackTask = TaskType(TYPE).nesteFallbackTask()
            val fallback = Task(nesteFallbackTask, task.payload, task.metadata)
            taskRepository.save(fallback)
        }
    }

    companion object {
        const val TYPE = "automatiskJournalfør"
    }
}

// TODO oppdatere metadata på denne tasken, eller en "resultatTask" hvis alt ok? Eller bare logge?
// task.metadata.apply {
//     this["behandleSakOppgaveId"] = response.behandleSakOppgaveId
//     this["behandlingId"] = response.behandlingId
//     this["fagsakId"] = response.fagsakId
// }
//  taskRepository.save(task)
// val feilCounter: Counter = Metrics.counter("alene.med.barn.automatiskjournalføring.feilet")

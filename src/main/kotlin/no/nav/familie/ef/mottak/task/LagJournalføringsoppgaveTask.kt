package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = LagJournalføringsoppgaveTask.TYPE,
                     maxAntallFeil = 100,
                     beskrivelse = "Lager oppgave i GoSys")
class LagJournalføringsoppgaveTask(private val taskRepository: TaskRepository,
                                   private val oppgaveService: OppgaveService,
                                   private val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        LOG.debug("Oppretter oppgave for søknad={}", task.payload)
        val uuid = try {
            UUID.fromString(task.payload)
        } catch (e: IllegalArgumentException) {
            null
        }

        if (uuid == null) {
            // Task fra hendelse
            oppgaveService.lagJournalføringsoppgaveForJournalpostId(task.payload)
        } else {
            // Task opprettet av ArkiverSøknadTask
            oppgaveService.lagJournalføringsoppgaveForSøknadId(task.payload)
        }
    }

    override fun onCompletion(task: Task) {
        val hentSaksnummerFraJoarkTask: Task =
                Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)

        if (featureToggleService.isEnabled("familie.ef.mottak.task-dittnav")) {
            val sendMeldingTilDittNavTask: Task =
                    Task.nyTask(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                                task.payload,
                                task.metadata)
            taskRepository.saveAll(listOf(hentSaksnummerFraJoarkTask, sendMeldingTilDittNavTask))
        } else {
            taskRepository.save(hentSaksnummerFraJoarkTask)
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(LagJournalføringsoppgaveTask::class.java)
        const val TYPE = "lagJournalføringsoppgave"
    }
}

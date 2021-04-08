package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = ArkiverSøknadTask.TYPE, beskrivelse = "Arkiver søknad")
class ArkiverSøknadTask(private val arkiveringService: ArkiveringService,
                        private val taskRepository: TaskRepository,
                        private val featureToggleService: FeatureToggleService,
                        private val soknadRepository: SoknadRepository) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val journalpostId = arkiveringService.journalførSøknad(task.payload)
        task.metadata.apply {
            this["journalpostId"] = journalpostId
        }
        taskRepository.save(task)
    }

    override fun onCompletion(task: Task) {
        val nesteTask = if (skalForsøkeÅOppretteSak(task)) {
            Task(LagBehandleSakOppgaveTask.TYPE, task.payload, task.metadata)
        } else {
            Task(LagJournalføringsoppgaveTask.TYPE, task.payload, task.metadata)
        }

        val sendMeldingTilDittNavTask =
            Task(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                task.payload,
                task.metadata)

        taskRepository.saveAll(listOf(nesteTask, sendMeldingTilDittNavTask))
    }

    private fun skalForsøkeÅOppretteSak(task: Task): Boolean {
        return erSøknadOmStønad(task.payload) && featureToggleService.isEnabled("familie.ef.mottak.opprett-sak")
    }

    private fun erSøknadOmStønad(søknadId: String): Boolean {
        val soknad = soknadRepository.findByIdOrNull(søknadId) ?: error("Søknad har forsvunnet!")
        return soknad.dokumenttype != DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
    }

    companion object {

        const val TYPE = "arkiverSøknad"
    }

}
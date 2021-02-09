package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = LagJournalføringsoppgaveTask.TYPE,
                     beskrivelse = "Lager oppgave i GoSys")
class LagJournalføringsoppgaveTask(private val taskRepository: TaskRepository,
                                   private val oppgaveService: OppgaveService,
                                   private val soknadRepository: SoknadRepository,
                                   private val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        require(gjelderSøknad(task))
        oppgaveService.lagJournalføringsoppgaveForSøknadId(task.payload)

    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task = when (skalForsøkeÅOppretteSak(task)) {
            true -> Task(LagBehandleSakOppgaveTask.TYPE, task.payload, task.metadata)
            false -> Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        }


        val sendMeldingTilDittNavTask =
                Task(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     task.payload,
                     task.metadata)
        taskRepository.saveAll(listOf(nesteTask, sendMeldingTilDittNavTask))
    }

    private fun skalForsøkeÅOppretteSak(task: Task): Boolean {
        return gjelderSøknad(task) && erSøknadOmStønad(task.payload) && featureToggleService.isEnabled("familie.ef.mottak.opprett-sak")
    }

    private fun erSøknadOmStønad(søknadId: String): Boolean {
        val soknad = soknadRepository.findByIdOrNull(søknadId) ?: error("Søknad har forsvunnet!")
        return soknad.dokumenttype != DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
    }

    private fun gjelderSøknad(task: Task): Boolean {
        return try {
            UUID.fromString(task.payload) // TODO AU! Kan dette gjøres mer elegant?
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(LagJournalføringsoppgaveTask::class.java)
        const val TYPE = "lagJournalføringsoppgave"
    }
}

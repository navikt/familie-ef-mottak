package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = LagJournalføringsoppgaveTask.TYPE,
                     beskrivelse = "Lager oppgave i GoSys")
class LagJournalføringsoppgaveTask(private val taskRepository: TaskRepository,
                                   private val oppgaveService: OppgaveService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        oppgaveService.lagJournalføringsoppgaveForSøknadId(task.payload)

    }

    override fun onCompletion(task: Task) {
        val hentSaksnummerFraJoarkTask = Task(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)

        val sendMeldingTilDittNavTask =
                Task(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     task.payload,
                     task.metadata)
        taskRepository.saveAll(listOf(hentSaksnummerFraJoarkTask, sendMeldingTilDittNavTask))
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(LagJournalføringsoppgaveTask::class.java)
        const val TYPE = "lagJournalføringsoppgave"
    }
}

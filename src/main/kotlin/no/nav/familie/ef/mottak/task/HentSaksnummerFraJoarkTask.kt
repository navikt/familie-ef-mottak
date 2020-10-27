package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 20,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 60 * 12)
class HentSaksnummerFraJoarkTask(private val hentJournalpostService: HentJournalpostService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        hentJournalpostService.hentSaksnummer(task.payload)
    }

    override fun onCompletion(task: Task) {

    }

    companion object {
        const val HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark"
    }
}
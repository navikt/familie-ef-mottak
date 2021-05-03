package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.TYPE,
                     maxAntallFeil = 20,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 60 * 12,
                     settTilManuellOppfølgning = true)
class HentSaksnummerFraJoarkTask(private val hentJournalpostService: HentJournalpostService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        hentJournalpostService.hentSaksnummer(task.payload)
    }

    companion object {

        const val TYPE = "hentSaksnummerFraJoark"
    }
}
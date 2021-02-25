package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SjekkOmJournalpostHarFåttEnSak.HENT_EKSTERN_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 20,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 60 * 12)
class SjekkOmJournalpostHarFåttEnSak(private val hentJournalpostService: HentJournalpostService) : AsyncTaskStep {

    override fun doTask(task: Task) {
        require(hentJournalpostService.harSaksnummer(task.payload))
    }

    companion object {

        const val HENT_EKSTERN_SAKSNUMMER_FRA_JOARK = "hentEksternSaksnummerFraJoark"
    }

}

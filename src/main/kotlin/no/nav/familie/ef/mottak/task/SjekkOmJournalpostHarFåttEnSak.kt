package no.nav.familie.ef.mottak.task

import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Service

@Deprecated("Trenger ikke å følge opp")
@Service
@TaskStepBeskrivelse(taskStepType = SjekkOmJournalpostHarFåttEnSak.TYPE,
                     maxAntallFeil = 20,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 60 * 12,
                     settTilManuellOppfølgning = true)
class SjekkOmJournalpostHarFåttEnSak() : AsyncTaskStep {

    override fun doTask(task: Task) {
    }

    companion object {

        const val TYPE = "hentEksternSaksnummerFraJoark"
    }

}

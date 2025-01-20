package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.ef.StønadType.BARNETILSYN
import no.nav.familie.kontrakter.felles.ef.StønadType.OVERGANGSSTØNAD
import no.nav.familie.kontrakter.felles.ef.StønadType.SKOLEPENGER
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = VelgAutomatiskEllerManuellFlytTask.TYPE,
    beskrivelse = "Velg automatisk eller manuell flyt",
)
class VelgAutomatiskEllerManuellFlytTask(
    val taskService: TaskService,
    val søknadskvitteringService: SøknadskvitteringService,
    val saksbehandlingClient: SaksbehandlingClient,
) : AsyncTaskStep {
    override fun doTask(task: Task) {
        val søknad: Søknad = søknadskvitteringService.hentSøknad(task.payload)
        val stønadstype: StønadType? = dokumenttypeTilStønadType(søknad.dokumenttype)

        val neste =
            if (skalAutomatiskJournalføre(stønadstype, søknad)) {
                automatiskJournalføringFlyt().first()
            } else {
                manuellJournalføringFlyt().first()
            }

        val nesteTask = Task(neste.type, task.payload, task.metadata)
        taskService.save(nesteTask)
    }

    private fun skalAutomatiskJournalføre(
        stønadstype: StønadType?,
        søknad: Søknad,
    ): Boolean =
        when (stønadstype) {
            OVERGANGSSTØNAD, BARNETILSYN, SKOLEPENGER ->
                saksbehandlingClient.kanOppretteFørstegangsbehandling(
                    søknad.fnr,
                    stønadstype,
                )
            else -> false
        }

    companion object {
        const val TYPE = "VelgAutomatiskEllerManuelFlyt"
    }
}

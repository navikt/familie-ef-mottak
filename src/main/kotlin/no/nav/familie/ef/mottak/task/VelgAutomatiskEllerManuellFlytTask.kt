package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.ef.StønadType.BARNETILSYN
import no.nav.familie.kontrakter.felles.ef.StønadType.OVERGANGSSTØNAD
import no.nav.familie.kontrakter.felles.ef.StønadType.SKOLEPENGER
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = VelgAutomatiskEllerManuellFlytTask.TYPE,
    beskrivelse = "Velg automatisk eller manuell flyt"
)
class VelgAutomatiskEllerManuellFlytTask(
    val taskRepository: TaskRepository,
    val søknadService: SøknadService,
    val saksbehandlingClient: SaksbehandlingClient,
    val featureToggleService: FeatureToggleService
) : AsyncTaskStep {

    override fun doTask(task: Task) {
        val søknad: Søknad = søknadService.get(task.payload)
        val stønadstype: StønadType? = dokumenttypeTilStønadType(søknad.dokumenttype)

        val neste =
            if (featureToggleService.isEnabled("familie.ef.mottak.automatisk.journalforing.ef-sak") &&
                skalAutomatiskJournalføre(stønadstype, søknad)
            ) {
                automatiskJournalføringFlyt().first()
            } else {
                manuellJournalføringFlyt().first()
            }

        val nesteTask = Task(neste.type, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    private fun skalAutomatiskJournalføre(
        stønadstype: StønadType?,
        søknad: Søknad
    ): Boolean {
        val kanAutomatiskJournalføre = when (stønadstype) {
            OVERGANGSSTØNAD, BARNETILSYN, SKOLEPENGER -> saksbehandlingClient.kanOppretteFørstegangsbehandling(
                søknad.fnr,
                stønadstype
            )
            else -> false
        }
        return kanAutomatiskJournalføre
    }

    companion object {
        const val TYPE = "VelgAutomatiskEllerManuelFlyt"
    }
}

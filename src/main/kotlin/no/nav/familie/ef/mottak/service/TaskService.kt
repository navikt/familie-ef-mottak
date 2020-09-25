package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.task.LagPdfTask
import no.nav.familie.ef.mottak.task.SendSøknadMottattTilDittNavTask.Companion.SEND_SØKNAD_MOTTATT_TIL_DITT_NAV
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class TaskService(private val taskRepository: TaskRepository,
                  private val soknadRepository: SoknadRepository,
                  private val featureToggleService: FeatureToggleService) {

    @Transactional
    fun startTaskProsessering(søknad: Soknad) {
        val properties =
                Properties().apply { this["søkersFødselsnummer"] = søknad.fnr }
                        .apply { this["dokumenttype"] = søknad.dokumenttype }
        taskRepository.save(Task.nyTask(LagPdfTask.LAG_PDF,
                                        søknad.id,
                                        properties))
        if (featureToggleService.isEnabled("familie.ef.mottak.task-dittnav")) {

            taskRepository.save(Task.nyTask(SEND_SØKNAD_MOTTATT_TIL_DITT_NAV,
                                            søknad.id,
                                            properties))
        }
        soknadRepository.save(søknad.copy(taskOpprettet = true))
    }

}


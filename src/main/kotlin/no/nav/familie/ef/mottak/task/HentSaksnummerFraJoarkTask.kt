package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 20,
                     beskrivelse = "Hent saksnummer fra joark",
                     triggerTidVedFeilISekunder = 60 * 60 * 12)
class HentSaksnummerFraJoarkTask(private val taskRepository: TaskRepository,
                                 private val hentJournalpostService: HentJournalpostService,
                                 private val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        hentJournalpostService.hentSaksnummer(task.payload)
    }

    override fun onCompletion(task: Task) {
        if (featureToggleService.isEnabled("familie.ef.mottak.send-til-sak")) {
            val nesteTask: Task =
                    Task.nyTask(SendSøknadTilSakTask.SEND_SØKNAD_TIL_SAK, task.payload, task.metadata)
            taskRepository.save(nesteTask)
        } else {
            logger.info("Sender ikke til sak, feature familie.ef.mottak.send-til-sak er skrudd av i Unleash ")
        }
    }

    companion object {

        const val HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark"
    }
}
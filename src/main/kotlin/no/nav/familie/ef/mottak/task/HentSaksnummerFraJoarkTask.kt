package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime

@Service
@TaskStepBeskrivelse(taskStepType = HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK,
                     maxAntallFeil = 100,
                     beskrivelse = "Hent saksnummer fra joark")
class HentSaksnummerFraJoarkTask(private val taskRepository: TaskRepository,
                                 private val hentJournalpostService: HentJournalpostService,
                                 private val featureToggleService: FeatureToggleService) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        try {
            hentJournalpostService.hentSaksnummer(task.payload)
        } catch (notFound: HttpClientErrorException.NotFound) {
            LOG.info("Hent saksnummer returnerte 404 response body={}",
                     notFound.responseBodyAsString)
            val copy = task.copy(triggerTid = LocalDateTime.now().plusMinutes(15))
            taskRepository.save(copy)
            throw notFound
        }
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
        private val LOG = LoggerFactory.getLogger(HentSaksnummerFraJoarkTask::class.java)
        const val HENT_SAKSNUMMER_FRA_JOARK = "hentSaksnummerFraJoark"
    }
}
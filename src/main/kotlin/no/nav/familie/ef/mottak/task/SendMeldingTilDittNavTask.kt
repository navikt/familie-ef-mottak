package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DittNavConfig
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(taskStepType = SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     beskrivelse = "Send melding til ditt nav")
class SendMeldingTilDittNavTask(
        private val taskRepository: TaskRepository,
        private val producer: DittNavKafkaProducer,
        private val søknadService: SøknadService,
        private val dittNavConfig: DittNavConfig
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)
    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val link = "${dittNavConfig.soknadfrontendUrl}/meldingmottatt/${task.payload}"
        producer.sendToKafka(søknad.fnr,
                             "Vi har mottatt søknad ",
                             task.payload,
                             task.id.toString(),
                             link) //TODO - bedre melding, ny link
        logger.info("Send melding til ditt nav ${task.payload}")
    }

    override fun onCompletion(task: Task) {
        val nesteTask: Task =
                Task.nyTask(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, task.payload, task.metadata)
        taskRepository.save(nesteTask)
    }

    companion object {

        const val SEND_MELDING_TIL_DITT_NAV = "sendMeldingTilDittNav"
    }

}
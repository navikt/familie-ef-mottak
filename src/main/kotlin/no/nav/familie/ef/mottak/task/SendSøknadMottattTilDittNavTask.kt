package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.SendSøknadMottattTilDittNavTask.Companion.TYPE
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = TYPE,
    beskrivelse = "Send 'søknad mottatt' til ditt nav",
)
class SendSøknadMottattTilDittNavTask(
    private val producer: DittNavKafkaProducer,
    private val søknadService: SøknadService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        producer.sendToKafka(
            søknad.fnr,
            lagLinkMelding(søknad.dokumenttype),
            task.payload,
            task.metadata["eventId"].toString(),
        )
        logger.info("Send melding til ditt nav søknadId=${task.payload}")
    }

    private fun lagLinkMelding(dokumenttype: String): String {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(dokumenttype)
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            return "Vi har mottatt skjema enslig mor eller far som er arbeidssøker."
        }
        return "Vi har mottatt søknaden din om ${søknadstypeTekst(søknadType)}."
    }

    private fun søknadstypeTekst(søknadType: SøknadType): String {
        return when (søknadType) {
            SøknadType.BARNETILSYN -> "stønad til barnetilsyn"
            SøknadType.OVERGANGSSTØNAD -> "overgangsstønad"
            SøknadType.SKOLEPENGER -> "stønad til skolepenger"
            else -> error("Kan mappe dokumenttype $søknadType til dittnav tekst")
        }
    }

    companion object {

        const val TYPE = "sendSøknadMottattTilDittNav"
    }
}

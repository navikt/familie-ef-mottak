package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DittNavConfig
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
@TaskStepBeskrivelse(taskStepType = SendMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     beskrivelse = "Send melding til ditt nav")
class SendMeldingTilDittNavTask(
        private val producer: DittNavKafkaProducer,
        private val søknadService: SøknadService,
        private val dittNavConfig: DittNavConfig
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private data class LinkMelding(val link: String, val melding: String)

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)

        val linkMelding = lagLinkMelding(UUID.fromString(søknad.id), søknadType)

        producer.sendToKafka(søknad.fnr,
                             linkMelding.melding,
                             task.payload,
                             task.id.toString(),
                             linkMelding.link)
        logger.info("Send melding til ditt nav søknadId=${task.payload}")
    }

    private fun link(søknadId: UUID) = "${dittNavConfig.soknadfrontendUrl}/innsendtsoknad?soknad=$søknadId"

    private fun lagLinkMelding(søknadId: UUID, søknadType: SøknadType): LinkMelding {
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            return LinkMelding("", "Vi har mottatt skjema enslig mor eller far som er arbeidssøker.")
        }

        val søknadstekst = søknadstypeTekst(søknadType)
        val dokumentasjonsbehov = søknadService.hentDokumentasjonsbehovForSøknad(søknadId).dokumentasjonsbehov
        return when {
            dokumentasjonsbehov.isEmpty() -> LinkMelding("", "Vi har mottatt søknaden din om $søknadstekst.")
            manglerVedlegg(dokumentasjonsbehov) ->
                LinkMelding(link(søknadId),
                            "Det ser ut til at det mangler noen vedlegg til søknaden din om $søknadstekst." +
                            " Se hva som mangler og last opp vedlegg.")
            else -> LinkMelding(link(søknadId), "Vi har mottatt søknaden din om $søknadstekst. Se vedleggene du lastet opp.")
        }
    }

    private fun manglerVedlegg(dokumentasjonsbehov: List<Dokumentasjonsbehov>) =
            dokumentasjonsbehov.any { !it.harSendtInn && it.opplastedeVedlegg.isEmpty() }

    private fun søknadstypeTekst(søknadType: SøknadType): String {
        return when (søknadType) {
            SøknadType.BARNETILSYN -> "stønad til barnetilsyn"
            SøknadType.OVERGANGSSTØNAD -> "overgangsstønad"
            SøknadType.SKOLEPENGER -> "stønad til skolepenger"
            else -> error("Kan mappe dokumenttype $søknadType til dittnav tekst")
        }
    }

    companion object {

        const val SEND_MELDING_TIL_DITT_NAV = "sendMeldingTilDittNav"
    }

}
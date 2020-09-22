package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DittNavConfig
import no.nav.familie.ef.mottak.repository.domain.Soknad
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
@TaskStepBeskrivelse(taskStepType = SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV,
                     beskrivelse = "Send melding til ditt nav")
class SendDokumentasjonsbehovMeldingTilDittNavTask(
        private val producer: DittNavKafkaProducer,
        private val søknadService: SøknadService,
        private val dittNavConfig: DittNavConfig
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private data class LinkMelding(val link: String, val melding: String)

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val dokumentasjonsbehov = søknadService.hentDokumentasjonsbehovForSøknad(UUID.fromString(søknad.id)).dokumentasjonsbehov
        if (dokumentasjonsbehov.isNotEmpty()) {
            val linkMelding = lagLinkMelding(søknad, dokumentasjonsbehov)

            producer.sendToKafka(søknad.fnr,
                                 linkMelding.melding,
                                 task.payload,
                                 task.id.toString(),
                                 linkMelding.link)
            logger.info("Send melding til ditt nav søknadId=${task.payload}")
        }

    }

    private fun link(søknadId: UUID) = "${dittNavConfig.soknadfrontendUrl}/innsendtsoknad?soknad=$søknadId"

    private fun lagLinkMelding(søknad: Soknad, dokumentasjonsbehov: List<Dokumentasjonsbehov>): LinkMelding {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        val søknadstekst = søknadstypeTekst(søknadType)
        val søknadId = UUID.fromString(søknad.id)
        return when {
            manglerVedlegg(dokumentasjonsbehov) -> {
                LinkMelding(link(søknadId),
                            "Det ser ut til at det mangler noen vedlegg til søknaden din om $søknadstekst." +
                            " Se hva som mangler og last opp vedlegg.")
            }
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
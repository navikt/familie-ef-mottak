package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
    beskrivelse = "Send dokumentasjonsbehovmelding til ditt nav"
)
class SendDokumentasjonsbehovMeldingTilDittNavTask(
    private val producer: DittNavKafkaProducer,
    private val søknadService: SøknadService,
    private val ettersendingConfig: EttersendingConfig,
    private val featureToggleService: FeatureToggleService
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private data class LinkMelding(val link: URL, val melding: String)

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            return
        }
        val dokumentasjonsbehov = søknadService.hentDokumentasjonsbehovForSøknad(søknad).dokumentasjonsbehov
        if (dokumentasjonsbehov.isNotEmpty()) {
            val linkMelding = lagLinkMelding(søknad, dokumentasjonsbehov)

            producer.sendToKafka(
                søknad.fnr,
                linkMelding.melding,
                task.payload,
                task.metadata["eventId"].toString(),
                linkMelding.link
            )
            logger.info("Send melding til ditt nav søknadId=${task.payload}")
        }
    }

    private fun lagLinkMelding(søknad: Søknad, dokumentasjonsbehov: List<Dokumentasjonsbehov>): LinkMelding {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        val søknadstekst = søknadstypeTekst(søknadType)

        return when {
            manglerVedlegg(dokumentasjonsbehov) -> {
                LinkMelding(
                    ettersendingConfig.ettersendingUrl,
                    "Det ser ut til at det mangler noen vedlegg til søknaden din om $søknadstekst." +
                        " Se hva som mangler og last opp vedlegg."
                )
            }
            else -> LinkMelding(
                ettersendingConfig.ettersendingUrl,
                "Vi har mottatt søknaden din om $søknadstekst. Se vedleggene du lastet opp."
            )
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

        const val TYPE = "sendMeldingTilDittNav"
    }
}

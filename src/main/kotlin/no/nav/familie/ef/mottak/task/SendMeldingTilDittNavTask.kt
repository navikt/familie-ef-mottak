package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.DittNavConfig
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
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
    override fun doTask(task: Task) {


        val søknad = søknadService.get(task.payload)
        val dokumentasjonsbehovForSøknad = søknadService.hentDokumentasjonsbehovForSøknad(UUID.fromString(søknad.id))

        val melding = lagMelding(dokumentasjonsbehovForSøknad, søknad)

        val link = "${dittNavConfig.soknadfrontendUrl}/innsendtsoknad/${task.payload}"
        producer.sendToKafka(søknad.fnr,
                             melding,
                             task.payload,
                             task.id.toString(),
                             link) //TODO - bedre melding, ny link
        logger.info("Send melding til ditt nav søknadId=${task.payload}")
    }

    private fun lagMelding(dokumentasjonsbehovForSøknad: DokumentasjonsbehovDto, søknad: Soknad): String {
        val søknadstype = when (SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)) {
            SøknadType.BARNETILSYN -> "barnetilsyn"
            SøknadType.OVERGANGSSTØNAD -> "overgangsstønad"
            SøknadType.SKOLEPENGER -> "skolepenger"
            else -> error("Kan mappe dokumenttype (SøknadType) til dittnav tekst")
        }

        return if (dokumentasjonsbehovForSøknad.dokumentasjonsbehov.isEmpty()) {
            "Vi har mottatt søknaden din om $søknadstype"
        } else {
            val manglerVedlegg =
                    dokumentasjonsbehovForSøknad.dokumentasjonsbehov.none { !it.harSendtInn && it.opplastedeVedlegg.isEmpty() }
            if (manglerVedlegg) {
                "Det ser ut til at det mangler noen vedlegg til søknaden din om $søknadstype. Se hva som mangler og last opp vedlegg."
            } else {
                "Vi har mottatt søknaden din om $søknadstype. Se vedleggene du lastet opp."
            }
        }
    }

    companion object {

        const val SEND_MELDING_TIL_DITT_NAV = "sendMeldingTilDittNav"
    }

}
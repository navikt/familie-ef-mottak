package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.ef.mottak.util.LinkMelding
import no.nav.familie.ef.mottak.util.lagMeldingManglerDokumentasjonsbehov
import no.nav.familie.ef.mottak.util.lagMeldingSøknadMottattBekreftelse
import no.nav.familie.ef.mottak.util.manglerVedlegg
import no.nav.familie.ef.mottak.util.tilDittNavTekst
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
    beskrivelse = "Send dokumentasjonsbehovmelding til ditt nav",
)
class SendDokumentasjonsbehovMeldingTilDittNavTask(
    private val producer: DittNavKafkaProducer,
    private val søknadskvitteringService: SøknadskvitteringService,
    private val taskService: TaskService,
    private val ettersendingConfig: EttersendingConfig,
) : AsyncTaskStep {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val søknad = søknadskvitteringService.hentSøknad(task.payload)
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            return
        }
        val dokumentasjonsbehov = søknadskvitteringService.hentDokumentasjonsbehovForSøknad(søknad).dokumentasjonsbehov
        if (dokumentasjonsbehov.isNotEmpty()) {
            val manglerVedleggPåSøknad = manglerVedlegg(dokumentasjonsbehov)

            if (manglerVedleggPåSøknad) {
                opprettSendPåminnelseTask(task)
            }

            val linkMelding = lagLinkMelding(søknad, manglerVedleggPåSøknad)

            // TODO: Husk å fjerne meg, samt tilsvarende avhengigheter.
            producer.sendToKafka(
                fnr = søknad.fnr,
                melding = linkMelding.melding,
                grupperingsnummer = task.payload,
                eventId = task.metadata["eventId"].toString(),
                link = linkMelding.link,
            )

            producer.sendBeskjedTilBruker(
                personIdent = søknad.fnr,
                varselId = task.metadata["eventId"].toString(),
                melding = linkMelding.melding,
                link = linkMelding.link.toString(),
            )

            logger.info("Send melding til ditt nav søknadId=${task.payload}")
        }
    }

    private fun opprettSendPåminnelseTask(task: Task) {
        taskService.save(
            SendPåminnelseOmDokumentasjonsbehovTilDittNavTask.opprettTask(task),
        )
    }

    private fun lagLinkMelding(
        søknad: Søknad,
        manglerVedleggPåSøknad: Boolean,
    ): LinkMelding {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        val søknadstekst = tilDittNavTekst(søknadType)

        return when {
            manglerVedleggPåSøknad -> {
                lagMeldingManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, søknadstekst)
            }

            else ->
                lagMeldingSøknadMottattBekreftelse(
                    ettersendingConfig.ettersendingUrl,
                    søknadstekst,
                )
        }
    }

    companion object {
        const val TYPE = "sendMeldingTilDittNav"
    }
}

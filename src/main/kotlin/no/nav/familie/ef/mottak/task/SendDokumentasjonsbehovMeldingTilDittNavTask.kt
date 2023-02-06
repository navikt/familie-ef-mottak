package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.SøknadService
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
import no.nav.familie.util.VirkedagerProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Properties
import java.util.UUID

@Service
@TaskStepBeskrivelse(
    taskStepType = SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE,
    beskrivelse = "Send dokumentasjonsbehovmelding til ditt nav",
)
class SendDokumentasjonsbehovMeldingTilDittNavTask(
    private val producer: DittNavKafkaProducer,
    private val søknadService: SøknadService,
    private val taskService: TaskService,
    private val ettersendingConfig: EttersendingConfig,
    private val featureToggleService: FeatureToggleService,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        if (søknadType == SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER) {
            return
        }
        val dokumentasjonsbehov = søknadService.hentDokumentasjonsbehovForSøknad(søknad).dokumentasjonsbehov
        if (dokumentasjonsbehov.isNotEmpty()) {
            val manglerVedleggPåSøknad = manglerVedlegg(dokumentasjonsbehov)

            if (manglerVedleggPåSøknad && featureToggleService.isEnabled("familie.ef.mottak.send-paminnelse-ditt-nav")) {
                opprettSendPåminnelseTask(task)
            }

            val linkMelding = lagLinkMelding(søknad, manglerVedleggPåSøknad)

            producer.sendToKafka(
                søknad.fnr,
                linkMelding.melding,
                task.payload,
                task.metadata["eventId"].toString(),
                linkMelding.link,
            )
            logger.info("Send melding til ditt nav søknadId=${task.payload}")
        }
    }

    private fun opprettSendPåminnelseTask(task: Task) {
        taskService.save(
            Task(
                SendPåminnelseOmDokumentasjonsbehovTilDittNavTask.TYPE,
                task.payload,
                Properties(task.metadata).apply {
                    this["eventId"] = UUID.randomUUID().toString()
                },
            ).medTriggerTid(VirkedagerProvider.nesteVirkedag(LocalDate.now().plusDays(2)).atTime(10, 0)),
        )
    }

    private fun lagLinkMelding(søknad: Søknad, manglerVedleggPåSøknad: Boolean): LinkMelding {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        val søknadstekst = tilDittNavTekst(søknadType)

        return when {
            manglerVedleggPåSøknad -> {
                lagMeldingManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, søknadstekst)
            }

            else -> lagMeldingSøknadMottattBekreftelse(
                ettersendingConfig.ettersendingUrl,
                søknadstekst,
            )
        }
    }

    companion object {

        const val TYPE = "sendMeldingTilDittNav"
    }
}

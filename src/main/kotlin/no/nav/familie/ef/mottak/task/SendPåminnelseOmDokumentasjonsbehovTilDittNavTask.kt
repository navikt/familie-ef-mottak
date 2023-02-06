package no.nav.familie.ef.mottak.task

import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.SendPåminnelseOmDokumentasjonsbehovTilDittNavTask.Companion.TYPE
import no.nav.familie.ef.mottak.util.LinkMelding
import no.nav.familie.ef.mottak.util.lagMeldingPåminnelseManglerDokumentasjonsbehov
import no.nav.familie.ef.mottak.util.tilDittNavTekst
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@TaskStepBeskrivelse(
    taskStepType = TYPE,
    beskrivelse = "Send påminnelse om manglende dokumentasjonsbehov til ditt nav",
)
class SendPåminnelseOmDokumentasjonsbehovTilDittNavTask(
    private val producer: DittNavKafkaProducer,
    private val søknadService: SøknadService,
    private val ettersendingService: EttersendingService,
    private val ettersendingConfig: EttersendingConfig,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val personIdent = PersonIdent(søknad.fnr)
        val søknader = søknadService.hentSøknaderForPerson(personIdent)
        val ettersendinger = ettersendingService.hentEttersendingerForPerson(personIdent)

        if (harSendtInnNySøknad(søknad, søknader) || harSendtInnEttersendingIEttertid(søknad, ettersendinger)) {
            logger.info("Sender ikke påminnelse til ditt nav om å sende inn ettersending søknadId=${task.payload}")
            return
        }

        val linkMelding = lagLinkMelding(søknad)
        producer.sendToKafka(
            søknad.fnr,
            linkMelding.melding,
            task.payload,
            task.metadata["eventId"].toString(),
            linkMelding.link,
        )
        logger.info("Sender påminnelse til ditt nav om å sende inn ettersending søknadId=${task.payload}")
    }

    private fun harSendtInnNySøknad(søknad: Søknad, søknader: List<Søknad>): Boolean =
        søknader.filter { it.id != søknad.id }
            .filter { SøknadType.hentSøknadTypeForDokumenttype(it.dokumenttype) != SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER }
            .any { it.opprettetTid > søknad.opprettetTid }

    private fun harSendtInnEttersendingIEttertid(søknad: Søknad, ettersendinger: List<Ettersending>): Boolean =
        ettersendinger.any { it.opprettetTid > søknad.opprettetTid }

    private fun lagLinkMelding(søknad: Søknad): LinkMelding {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        val søknadstekst = tilDittNavTekst(søknadType)

        return lagMeldingPåminnelseManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, søknadstekst)
    }

    companion object {

        const val TYPE = "SendPåminnelseOmDokumentasjonTilDittNav"
    }
}

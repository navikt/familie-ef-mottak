package no.nav.familie.ef.mottak.task

import io.micrometer.core.instrument.Metrics.counter
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.SendPåminnelseOmDokumentasjonsbehovTilDittNavTask.Companion.TYPE
import no.nav.familie.ef.mottak.util.LinkMelding
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.ef.mottak.util.lagMeldingPåminnelseManglerDokumentasjonsbehov
import no.nav.familie.ef.mottak.util.tilDittNavTekst
import no.nav.familie.kontrakter.ef.søknad.KanSendePåminnelseRequest
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.prosessering.AsyncTaskStep
import no.nav.familie.prosessering.TaskStepBeskrivelse
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.util.VirkedagerProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Properties
import java.util.UUID

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
    private val saksbehandlingClient: SaksbehandlingClient,
) : AsyncTaskStep {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val dokumentasjonsbehovvarslinger = counter("alene.med.barn.dokumentasjonsbehovvarsling.sendt")
    val dokumentasjonsbehovvarslingerBrukerSendtInn =
        counter("alene.med.barn.dokumentasjonsbehovvarslingstoppet.bruker")
    val dokumentasjonsbehovvarslingerSaksbehandlerStartet =
        counter("alene.med.barn.dokumentasjonsbehovvarslingstoppet.saksbehandler")
    val dokumentasjonsbehovvarslingerSaksbehandlerOgBruker =
        counter("alene.med.barn.dokumentasjonsbehovvarslingstoppet.saksbehandlerogbruker")

    override fun doTask(task: Task) {
        val søknad = søknadService.get(task.payload)
        val personIdent = PersonIdent(søknad.fnr)
        val søknader = søknadService.hentSøknaderForPerson(personIdent)
        val ettersendinger = ettersendingService.hentEttersendingerForPerson(personIdent)
        val stønadstype: StønadType? = dokumenttypeTilStønadType(søknad.dokumenttype)

        if (skalIkkeSendePåminnelse(søknad, søknader, ettersendinger, stønadstype)) {
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
            PreferertKanal.SMS,
        )
        dokumentasjonsbehovvarslinger.increment()
        logger.info("Sender påminnelse til ditt nav om å sende inn ettersending søknadId=${task.payload}")
    }

    private fun skalIkkeSendePåminnelse(
        søknad: Søknad,
        søknader: List<Søknad>,
        ettersendinger: List<Ettersending>,
        stønadType: StønadType?,
    ): Boolean {
        if (stønadType == null) {
            return true
        }

        val brukerHarSendtInnNoe =
            harSendtInnNySøknad(søknad, søknader) || harSendtInnEttersendingIEttertid(søknad, ettersendinger)
        val tilhørendeBehandleSakOppgaveErPåbegynt = tilhørendeBehandleSakOppgaveErPåbegynt(søknad, stønadType)

        loggMicrometer(brukerHarSendtInnNoe, tilhørendeBehandleSakOppgaveErPåbegynt)

        return brukerHarSendtInnNoe ||
            tilhørendeBehandleSakOppgaveErPåbegynt
    }

    private fun loggMicrometer(
        brukerHarSendtInnNoe: Boolean,
        saksbehandlerHarStartet: Boolean,
    ) {
        if (brukerHarSendtInnNoe and !saksbehandlerHarStartet) {
            dokumentasjonsbehovvarslingerBrukerSendtInn.increment()
        }

        if (saksbehandlerHarStartet and !brukerHarSendtInnNoe) {
            dokumentasjonsbehovvarslingerSaksbehandlerStartet.increment()
        }

        if (brukerHarSendtInnNoe and saksbehandlerHarStartet) {
            dokumentasjonsbehovvarslingerSaksbehandlerOgBruker.increment()
        }
    }

    private fun harSendtInnNySøknad(søknad: Søknad, søknader: List<Søknad>): Boolean =
        søknader.filter { it.id != søknad.id }
            .filter { SøknadType.hentSøknadTypeForDokumenttype(it.dokumenttype) != SøknadType.OVERGANGSSTØNAD_ARBEIDSSØKER }
            .any { it.opprettetTid > søknad.opprettetTid }

    private fun harSendtInnEttersendingIEttertid(søknad: Søknad, ettersendinger: List<Ettersending>): Boolean =
        ettersendinger.any { it.opprettetTid > søknad.opprettetTid }

    private fun tilhørendeBehandleSakOppgaveErPåbegynt(søknad: Søknad, stønadType: StønadType): Boolean =
        saksbehandlingClient.kanSendePåminnelseTilBruker(
            KanSendePåminnelseRequest(
                personIdent = søknad.fnr,
                stønadType = stønadType,
                innsendtSøknadTidspunkt = søknad.opprettetTid,
            ),
        )

    private fun lagLinkMelding(søknad: Søknad): LinkMelding {
        val søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype)
        val søknadstekst = tilDittNavTekst(søknadType)

        return lagMeldingPåminnelseManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, søknadstekst)
    }

    companion object {

        const val TYPE = "SendPåminnelseOmDokumentasjonTilDittNav"

        fun opprettTask(task: Task) = Task(
            TYPE,
            task.payload,
            Properties(task.metadata).apply {
                this["eventId"] = UUID.randomUUID().toString()
            },
        ).medTriggerTid(VirkedagerProvider.nesteVirkedag(LocalDate.now().plusDays(2)).atTime(10, 0))
    }
}

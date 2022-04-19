package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.IOTestUtil
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariant
import no.nav.familie.kontrakter.felles.journalpost.Dokumentvariantformat
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.journalpost.Sak
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientResponseException
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

internal class OppgaveServiceTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val sakService: SakService = mockk()
    private val opprettOppgaveMapper = spyk(OpprettOppgaveMapper(integrasjonerClient))
    private val saksbehandlingClient = mockk<SaksbehandlingClient>()
    private val ettersendingService = mockk<EttersendingService>()

    private val oppgaveService: OppgaveService =
            OppgaveService(integrasjonerClient = integrasjonerClient,
                           featureToggleService = mockk(relaxed = true),
                           søknadService = søknadService,
                           opprettOppgaveMapper = opprettOppgaveMapper,
                           sakService = sakService,
                           saksbehandlingClient = saksbehandlingClient,
                           ettersendingService = ettersendingService)

    @BeforeEach
    private fun init() {
        every { integrasjonerClient.hentAktørId(any()) } returns Testdata.randomAktørId()
        every { integrasjonerClient.hentIdentForAktørId(any()) } returns Testdata.randomFnr()
        every { integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(any()) } returns listOf(Enhet(enhetId = "4489", enhetNavn = "NAY"))
        every { integrasjonerClient.lagOppgave(any()) } returns OppgaveResponse(oppgaveId = 1)
    }

    @Test
    fun `Skal kalle integrasjonsklient ved opprettelse av oppgave`() {
        every { integrasjonerClient.hentJournalpost("999") }
                .returns(Journalpost("999",
                                     Journalposttype.I,
                                     Journalstatus.MOTTATT,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     listOf(DokumentInfo("1", "", "", null, null, null)),
                                     null))
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0L, emptyList())
        every {
            søknadService.get("123")
        } returns Søknad(søknadJson = EncryptedString("{}"),
                         dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr())


        oppgaveService.lagJournalføringsoppgaveForSøknadId("123")

        verify(exactly = 1) {
            integrasjonerClient.lagOppgave(any())
        }
    }

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettOppgave-kall får feil som følge av at enhet ikke blir funnet for bruker`() {

        val opprettOppgaveRequest =
                opprettOppgaveMapper.toJournalføringsoppgave(journalpost, BehandlesAvApplikasjon.INFOTRYGD, "4489")

        every {
            integrasjonerClient.lagOppgave(opprettOppgaveRequest)
        } throws lagRessursException()

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse = oppgaveService.lagJournalføringsoppgave(journalpost, BehandlesAvApplikasjon.INFOTRYGD)


        assertEquals(1, oppgaveResponse)
    }

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettBahandleSak-kall får feil når enhet ikke blir funnet for bruker`() {

        every { integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(any()) } returns emptyList()
        val behandleSakOppgaveRequest =
                opprettOppgaveMapper.toBehandleSakOppgave(journalpost, BehandlesAvApplikasjon.INFOTRYGD, null)

        every {
            integrasjonerClient.lagOppgave(behandleSakOppgaveRequest)
        } throws lagRessursException()

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse = oppgaveService.lagBehandleSakOppgave(journalpost, BehandlesAvApplikasjon.INFOTRYGD)


        assertEquals(1, oppgaveResponse)
    }

    private fun lagRessursException(): RessursException {
        val httpServerErrorException = HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                                "Server error",
                                                                IOTestUtil.readFile("opprett_oppgave_feilet.json").toByteArray(),
                                                                Charset.defaultCharset())
        return lesRessurs(httpServerErrorException)?.let { RessursException(it, httpServerErrorException) }!!
    }


    private fun lesRessurs(e: RestClientResponseException): Ressurs<Any>? {
        return try {
            if (e.responseBodyAsString.contains("status")) {
                objectMapper.readValue<Ressurs<Any>>(e.responseBodyAsString)
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }

    @Test
    internal fun `lagJournalføringsoppgaveForJournalpostId skal sette behandlesAvApplikasjon=UAVKLART hvis det finnes en behandling i ny løsning`() {
        val journalpostId = UUID.randomUUID().toString()
        val journalpost = journalpost.copy(bruker = Bruker("1", type = BrukerIdType.FNR), journalpostId = journalpostId)

        every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpost
        every { saksbehandlingClient.finnesBehandlingForPerson("1", isNull()) } returns true
        every { integrasjonerClient.finnOppgaver(journalpostId, any()) } returns FinnOppgaveResponseDto(0, emptyList())
        every { sakService.finnesIkkeIInfotrygd(any(), any()) } returns false

        oppgaveService.lagJournalføringsoppgaveForJournalpostId(journalpostId)

        verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.UAVKLART, "4489") }
    }

    @Test
    internal fun `lagJournalføringsoppgaveForJournalpostId skal sette behandlesAvApplikasjon=INFOTRYGD hvis det ikke finnes en behandling i ny løsning`() {
        val journalpostId = UUID.randomUUID().toString()
        val journalpost = journalpost.copy(bruker = Bruker("1", type = BrukerIdType.FNR), journalpostId = journalpostId)

        every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpost
        every { saksbehandlingClient.finnesBehandlingForPerson("1", isNull()) } returns false
        every { integrasjonerClient.finnOppgaver(journalpostId, any()) } returns FinnOppgaveResponseDto(0, emptyList())

        oppgaveService.lagJournalføringsoppgaveForJournalpostId(journalpostId)

        verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.INFOTRYGD, "4489") }
    }

    @Test
    internal fun `lagJournalføringsoppgaveForEttersending skal opprette en oppgave for ny løsning dersom det finnes en behandling i ny løsning`() {
        every { ettersendingService.hentEttersending(ettersendingId) } returns ettersending
        every { saksbehandlingClient.finnesBehandlingForPerson(ettersending.fnr, StønadType.OVERGANGSSTØNAD) } returns true
        every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
        oppgaveService.lagJournalføringsoppgaveForEttersendingId(ettersendingId)

        verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.EF_SAK, "4489") }
    }

    @Test
    internal fun `lagJournalføringsoppgaveForEttersending skal opprette oppgave med behandlesAvApplikasjon=EF_SAK_INFOTRYGD dersom det ikke finnes en behandling i ny løsning`() {
        every { ettersendingService.hentEttersending(ettersendingId) } returns ettersending
        every { saksbehandlingClient.finnesBehandlingForPerson(ettersending.fnr, StønadType.OVERGANGSSTØNAD) } returns false
        every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
        every { sakService.finnesIkkeIInfotrygd(any(), any()) } returns true
        oppgaveService.lagJournalføringsoppgaveForEttersendingId(ettersendingId)

        verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.EF_SAK_INFOTRYGD, "4489") }
    }

    @Test
    internal fun `lagJournalføringsoppgaveForEttersending skal opprette en oppgave med behandlesAvApplikasjon=INFOTRYGD dersom finnes en sak mot infotrygd fra før`() {
        every { ettersendingService.hentEttersending(ettersendingId) } returns ettersending
        every { saksbehandlingClient.finnesBehandlingForPerson(ettersending.fnr, StønadType.OVERGANGSSTØNAD) } returns false
        every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
        every { sakService.finnesIkkeIInfotrygd(any(), any()) } returns false
        oppgaveService.lagJournalføringsoppgaveForEttersendingId(ettersendingId)

        verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.INFOTRYGD, "4489") }
    }

    private val ettersendingId = UUID.randomUUID().toString()
    private val journalpost =
            Journalpost(
                    journalpostId = "111111111",
                    journalposttype = Journalposttype.I,
                    journalstatus = Journalstatus.MOTTATT,
                    tema = "ENF",
                    behandlingstema = "ab0071",
                    tittel = "abrakadabra",
                    bruker = Bruker(type = BrukerIdType.AKTOERID, id = "3333333333333"),
                    journalforendeEnhet = "4817",
                    kanal = "SKAN_IM",
                    sak = Sak(null, null, null),
                    dokumenter =
                    listOf(
                            DokumentInfo(
                                    dokumentInfoId = "12345",
                                    tittel = "Tittel",
                                    brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                                    dokumentvarianter = listOf(Dokumentvariant(variantformat = Dokumentvariantformat.ARKIV))
                            )
                    )
            )

    private val ettersending = Ettersending(id = UUID.randomUUID(),
                                            ettersendingJson = EncryptedString(""),
                                            ettersendingPdf = EncryptedFile("abc".toByteArray()),
                                            stønadType = "OVERGANGSSTØNAD",
                                            journalpostId = "123Abc",
                                            fnr = "12345678901",
                                            taskOpprettet = true,
                                            opprettetTid = LocalDateTime.of(2021, 5, 1, 13, 2)

    )

}
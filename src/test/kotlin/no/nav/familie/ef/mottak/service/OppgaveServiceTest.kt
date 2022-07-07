package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.IOTestUtil
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.felles.Behandlingstema
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
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
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
    private val opprettOppgaveMapper = spyk(OpprettOppgaveMapper(integrasjonerClient))
    private val ettersendingService = mockk<EttersendingService>()

    private val oppgaveService: OppgaveService =
        OppgaveService(
            integrasjonerClient = integrasjonerClient,
            søknadService = søknadService,
            opprettOppgaveMapper = opprettOppgaveMapper,
            ettersendingService = ettersendingService
        )

    @BeforeEach
    private fun init() {
        every { integrasjonerClient.hentAktørId(any()) } returns Testdata.randomAktørId()
        every { integrasjonerClient.hentIdentForAktørId(any()) } returns Testdata.randomFnr()
        every { integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(any()) } returns listOf(
            Enhet(
                enhetId = "4489",
                enhetNavn = "NAY"
            )
        )
        every { integrasjonerClient.lagOppgave(any()) } returns OppgaveResponse(oppgaveId = 1)
        every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
            antallTreffTotalt = 1,
            mapper = listOf(
                MappeDto(
                    id = 123,
                    navn = "EF Sak 01",
                    enhetsnr = ""
                ),
                MappeDto(
                    id = 456,
                    navn = "EF Sak - 65 Opplæring",
                    enhetsnr = ""
                )
            )
        )
        every { integrasjonerClient.oppdaterOppgave(any(), any()) } returns 123
    }

    @Test
    fun `Skal kalle integrasjonsklient ved opprettelse av oppgave`() {
        every { integrasjonerClient.hentJournalpost("999") }
            .returns(
                Journalpost(
                    "999",
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
                    null
                )
            )
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0L, emptyList())
        every {
            søknadService.get("123")
        } returns Søknad(
            søknadJson = EncryptedString("{}"),
            dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
            journalpostId = "999",
            fnr = Testdata.randomFnr()
        )

        oppgaveService.lagJournalføringsoppgaveForSøknadId("123")

        verify(exactly = 1) {
            integrasjonerClient.lagOppgave(any())
        }
    }

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettOppgave-kall får feil som følge av at enhet ikke blir funnet for bruker`() {

        val opprettOppgaveRequest =
            opprettOppgaveMapper.toJournalføringsoppgave(journalpostOvergangsstøand, BehandlesAvApplikasjon.INFOTRYGD, "4489")

        every {
            integrasjonerClient.lagOppgave(opprettOppgaveRequest)
        } throws lagRessursException()

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse =
            oppgaveService.lagJournalføringsoppgave(journalpostOvergangsstøand, BehandlesAvApplikasjon.INFOTRYGD)

        assertEquals(1, oppgaveResponse)
    }

    private fun lagRessursException(): RessursException {
        val httpServerErrorException = HttpServerErrorException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Server error",
            IOTestUtil.readFile("opprett_oppgave_feilet.json").toByteArray(),
            Charset.defaultCharset()
        )
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

    @Nested
    inner class LagJournalføringsoppgaveForSøknadId {

        @Test
        fun `skal opprette en ny oppgave for søknad`() {
            val søknadId = "enSøknadId"
            val journalpostId = "999"
            every { søknadService.get(søknadId) } returns Søknad(
                søknadJson = EncryptedString("{}"),
                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                journalpostId = journalpostId,
                fnr = Testdata.randomFnr(),
                behandleINySaksbehandling = true
            )
            every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpostOvergangsstøand
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())

            oppgaveService.lagJournalføringsoppgaveForSøknadId(søknadId)

            verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.EF_SAK, "4489") }
        }
    }

    @Nested
    inner class LagJournalføringsoppgaveForJournalpostId {

        @Test
        fun `skal sette behandlesAvApplikasjon=UAVKLART hvis det finnes en behandling i ny løsning`() {
            val journalpostId = UUID.randomUUID().toString()
            val journalpost =
                journalpostOvergangsstøand.copy(bruker = Bruker("1", type = BrukerIdType.FNR), journalpostId = journalpostId)

            every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpost
            every { integrasjonerClient.finnOppgaver(journalpostId, any()) } returns FinnOppgaveResponseDto(0, emptyList())

            oppgaveService.lagJournalføringsoppgaveForJournalpostId(journalpostId)

            verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.UAVKLART, "4489") }
        }
    }

    @Nested
    inner class LagJournalføringsoppgaveForEttersending {

        @Test
        fun `skal opprette en oppgave for ny løsning dersom det finnes en behandling i ny løsning`() {
            every { ettersendingService.hentEttersending(ettersendingId) } returns ettersending
            every { integrasjonerClient.hentJournalpost(any()) } returns journalpostOvergangsstøand
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            oppgaveService.lagJournalføringsoppgaveForEttersendingId(ettersendingId)

            verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.EF_SAK, "4489") }
        }
    }

    @Nested
    inner class OppdaterOppgaveMedRiktigMappeId {

        @Test
        fun `skal ikke flytte oppgave til mappe hvis sak ikke kan behandles i ny løsning for barnetilsyn`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = Behandlingstema.Barnetilsyn,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId)

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal flytte oppgave til mappe hvis sak kan behandles i ny løsning for barnetilsyn`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = Behandlingstema.Barnetilsyn,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId)

            verify(exactly = 1) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal flytte oppgave til opplæringsmappe hvis skolepenger og skal behandles i ny løsning`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdOpplæring = 456

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = listOf(
                    MappeDto(
                        id = mappeIdOpplæring,
                        navn = "EF Sak - 65 Opplæring",
                        enhetsnr = ""
                    )
                )
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = Behandlingstema.Skolepenger,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId)

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdOpplæring.toLong())
        }

        @Test
        fun `skal ikke flytte oppgave til mappe hvis behandlingstema er null (arbeidssøkerskjema)`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = null,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId)

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal flytte oppgave til EF Sak 01 hvis barnetilsyn og skal behandles i ny løsning`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()

            val mappeidUplassert = 123
            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = listOf(
                    MappeDto(
                        id = mappeidUplassert,
                        navn = "EF Sak 01",
                        enhetsnr = ""
                    ),
                )
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = Behandlingstema.Barnetilsyn,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId)

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeidUplassert.toLong())
        }

        @Test
        fun `skal flytte oppgave til uplassertMappe hvis overgangsstønad`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdUplassert = 123

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = listOf(
                    MappeDto(
                        id = mappeIdUplassert,
                        navn = "EF Sak 01",
                        enhetsnr = ""
                    )
                )
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = Behandlingstema.Overgangsstønad,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId)

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdUplassert.toLong())
        }
    }

    private val ettersendingId = UUID.randomUUID().toString()
    private val journalpostOvergangsstøand =
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

    private val ettersending = Ettersending(
        id = UUID.randomUUID(),
        ettersendingJson = EncryptedString(""),
        ettersendingPdf = EncryptedFile("abc".toByteArray()),
        stønadType = "OVERGANGSSTØNAD",
        journalpostId = "123Abc",
        fnr = "12345678901",
        taskOpprettet = true,
        opprettetTid = LocalDateTime.of(2021, 5, 1, 13, 2)

    )

    private fun lagOppgaveForFordeling(
        behandlingstema: Behandlingstema?,
        behandlesAvApplikasjon: BehandlesAvApplikasjon
    ) =
        Oppgave(
            id = 123L,
            behandlingstema = behandlingstema?.value,
            status = StatusEnum.OPPRETTET,
            tildeltEnhetsnr = "4489",
            behandlesAvApplikasjon = behandlesAvApplikasjon.applikasjon
        )
}

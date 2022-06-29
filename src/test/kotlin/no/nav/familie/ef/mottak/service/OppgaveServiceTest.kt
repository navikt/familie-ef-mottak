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
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.mapper.BehandlesAvApplikasjon
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.IOTestUtil
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.ef.søknad.Aktivitet
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.ef.StønadType
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
    private val sakService: SakService = mockk()
    private val opprettOppgaveMapper = spyk(OpprettOppgaveMapper(integrasjonerClient))
    private val saksbehandlingClient = mockk<SaksbehandlingClient>()
    private val ettersendingService = mockk<EttersendingService>()
    private val featureToggleService = mockk<FeatureToggleService>()

    private val oppgaveService: OppgaveService =
        OppgaveService(
            integrasjonerClient = integrasjonerClient,
            søknadService = søknadService,
            opprettOppgaveMapper = opprettOppgaveMapper,
            ettersendingService = ettersendingService,
            featureToggleService = featureToggleService
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
                    navn = "EF Sak 01 Uplassert",
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
        every { featureToggleService.isEnabled(any()) } returns true
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
            opprettOppgaveMapper.toJournalføringsoppgave(
                journalpostOvergangsstøand,
                BehandlesAvApplikasjon.INFOTRYGD,
                "4489"
            )

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

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettBehandleSak-kall får feil når enhet ikke blir funnet for bruker`() {

        every { integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(any()) } returns emptyList()
        val behandleSakOppgaveRequest =
            opprettOppgaveMapper.toBehandleSakOppgave(
                journalpostOvergangsstøand,
                BehandlesAvApplikasjon.INFOTRYGD,
                null
            )

        every {
            integrasjonerClient.lagOppgave(behandleSakOppgaveRequest)
        } throws lagRessursException()

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse =
            oppgaveService.lagBehandleSakOppgave(journalpostOvergangsstøand, BehandlesAvApplikasjon.INFOTRYGD)

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
            every { saksbehandlingClient.finnesBehandlingForPerson(any(), StønadType.OVERGANGSSTØNAD) } returns false
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            every { sakService.finnesIkkeIInfotrygd(any()) } returns false

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
                journalpostOvergangsstøand.copy(
                    bruker = Bruker("1", type = BrukerIdType.FNR),
                    journalpostId = journalpostId
                )

            every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpost
            every { saksbehandlingClient.finnesBehandlingForPerson("1", isNull()) } returns true
            every { integrasjonerClient.finnOppgaver(journalpostId, any()) } returns FinnOppgaveResponseDto(
                0,
                emptyList()
            )
            every { sakService.finnesIkkeIInfotrygd(any(), any()) } returns false

            oppgaveService.lagJournalføringsoppgaveForJournalpostId(journalpostId)

            verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.UAVKLART, "4489") }
        }
    }

    @Nested
    inner class LagJournalføringsoppgaveForEttersending {

        @Test
        fun `skal opprette en oppgave for ny løsning dersom det finnes en behandling i ny løsning`() {
            every { ettersendingService.hentEttersending(ettersendingId) } returns ettersending
            every {
                saksbehandlingClient.finnesBehandlingForPerson(
                    ettersending.fnr,
                    StønadType.OVERGANGSSTØNAD
                )
            } returns true
            every { integrasjonerClient.hentJournalpost(any()) } returns journalpostOvergangsstøand
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            oppgaveService.lagJournalføringsoppgaveForEttersendingId(ettersendingId)

            verify { opprettOppgaveMapper.toJournalføringsoppgave(any(), BehandlesAvApplikasjon.EF_SAK, "4489") }
        }
    }

    @Nested
    inner class OppdaterOppgaveMedRiktigMappeId {

        // Saker med BehandlesAvApplikasjon=INFOTRYGD skal migreres og behandles i ny løsning for overgangsstønad
        @Test
        fun `skal flytte overgangsstønad-oppgave til mappe selv om BehandlesAvApplikasjon er satt til infotrygd`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_OVERGANGSSTØNAD,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

            verify(exactly = 1) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal ikke flytte oppgave til mappe hvis sak ikke kan behandles i ny løsning for barnetilsyn`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_BARNETILSYN,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal flytte oppgave til mappe hvis sak kan behandles i ny løsning for barnetilsyn`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_BARNETILSYN,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

            verify(exactly = 1) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal flytte oppgave til mappe for selvstendige hvis søknad har firma`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdSelvstendig = 456

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = lagMapper(mappeIdSelvstendig = 456)
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_OVERGANGSSTØNAD,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.get("123") } returns søknadOvergangsstønad(erSelvstendig = true)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdSelvstendig.toLong())
        }

        @Test
        fun `skal flytte oppgave til mappe for særlig tilsynskrevende barn for overgangsstønad`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdTilsynskrevende = 3456

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = lagMapper(mappeIdTilsynskrevende = mappeIdTilsynskrevende)
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_OVERGANGSSTØNAD,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.get("123") } returns søknadOvergangsstønad(erSelvstendig = true, harTilsynskrevendeBarn = true)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdTilsynskrevende.toLong())
        }

        @Test
        fun `skal flytte oppgave til mappe for særlig tilsynskrevende barn for barnetilsyn`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdTilsynskrevende = 3456

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = lagMapper(mappeIdTilsynskrevende = mappeIdTilsynskrevende)
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_BARNETILSYN,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.get("123") } returns søknadBarnetilsyn(erSelvstendig = true, harTilsynskrevendeBarn = true)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdTilsynskrevende.toLong())
        }

        @Test
        fun `skal flytte barnetilsyn-oppgave til mappe for selvstendig `() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdSelvstendig = 456

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = lagMapper(mappeIdTilsynskrevende = mappeIdSelvstendig)
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_BARNETILSYN,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.get("123") } returns søknadBarnetilsyn(erSelvstendig = true, harTilsynskrevendeBarn = false)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdSelvstendig.toLong())
        }

        @Test
        fun `skal flytte oppgave til uplassert hvis vanlig søknad `() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val mappeIdUplassert = 123

            every { integrasjonerClient.finnMappe(any()) } returns FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper = lagMapper(mappeIdUplassert = mappeIdUplassert)
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_OVERGANGSSTØNAD,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.get("123") } returns søknadOvergangsstønad(false)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdUplassert.toLong())
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
                behandlingstema = BEHANDLINGSTEMA_SKOLEPENGER,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdOpplæring.toLong())
        }

        @Test
        fun `skal ikke flytte oppgave til mappe hvis behandlingstema er null (arbeidssøkerskjema)`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = null,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.INFOTRYGD
            )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

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
                        navn = "EF Sak 01 Uplassert",
                        enhetsnr = ""
                    ),
                )
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_BARNETILSYN,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

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
                        navn = "EF Sak 01 Uplassert",
                        enhetsnr = ""
                    )
                )
            )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns lagOppgaveForFordeling(
                behandlingstema = BEHANDLINGSTEMA_OVERGANGSSTØNAD,
                behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK_INFOTRYGD
            )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, null)

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(mappeIdUplassert.toLong())
        }

        private fun søknadOvergangsstønad(erSelvstendig: Boolean = false, harTilsynskrevendeBarn: Boolean = false): Søknad {

            val startSøknadMedAlt = Testdata.søknadOvergangsstønad

            val situasjon = when (harTilsynskrevendeBarn) {
                false -> startSøknadMedAlt.situasjon.verdi.copy(barnMedSærligeBehov = null)
                true -> startSøknadMedAlt.situasjon.verdi
            }

            val aktivitet = when (erSelvstendig) {
                true -> startSøknadMedAlt.aktivitet
                false -> Søknadsfelt("aktivitet", tomAktivitet)
            }

            val søknadOvergangsstønad =
                startSøknadMedAlt.copy(
                    aktivitet = aktivitet,
                    situasjon = Søknadsfelt("s", situasjon)
                )

            val søknad = SøknadMedVedlegg(
                søknad = søknadOvergangsstønad,
                vedlegg = listOf(),
                dokumentasjonsbehov = listOf(),
                behandleINySaksbehandling = true
            )
            return SøknadMapper.fromDto(søknad.søknad, true)
        }
    }

    private fun søknadBarnetilsyn(erSelvstendig: Boolean = false, harTilsynskrevendeBarn: Boolean = false): Søknad {

        val startSøknadMedAlt = Testdata.søknadBarnetilsyn

        val barn = when (harTilsynskrevendeBarn) {
            true -> startSøknadMedAlt.barn.verdi.first()
                .copy(særligeTilsynsbehov = Søknadsfelt("Særlige tilsynsbehov", "har særlige tilsynsbehov"))
            false -> startSøknadMedAlt.barn.verdi.first()
        }

        val aktivitet = when (erSelvstendig) {
            true -> startSøknadMedAlt.aktivitet
            false -> Søknadsfelt("aktivitet", tomAktivitet)
        }

        val søknadBarnetilsyn =
            startSøknadMedAlt.copy(
                aktivitet = aktivitet,
                barn = Søknadsfelt("Barn", listOf(barn))
            )

        val søknad = SøknadMedVedlegg(
            søknad = søknadBarnetilsyn,
            vedlegg = listOf(),
            dokumentasjonsbehov = listOf(),
            behandleINySaksbehandling = true
        )
        return SøknadMapper.fromDto(søknad.søknad, true)
    }

    val tomAktivitet = Aktivitet(
        hvordanErArbeidssituasjonen = Søknadsfelt(
            "Hvordan er arbeidssituasjonen din?",
            listOf(
                "Jeg er hjemme med barn under 1 år",
                "Jeg er i arbeid",
                "Jeg er selvstendig næringsdrivende eller frilanser"
            )
        ),
        arbeidsforhold = null,
        selvstendig = null,
        firmaer = null,
        virksomhet = null,
        arbeidssøker = null,
        underUtdanning = null,
        aksjeselskap = null,
        erIArbeid = null,
        erIArbeidDokumentasjon = null
    )

    private fun lagMapper(
        mappeIdSelvstendig: Int = 456,
        mappeIdUplassert: Int = 123,
        mappeIdTilsynskrevende: Int = 765
    ) = listOf(
        MappeDto(
            id = 987,
            navn = "EF Sak - 65 Opplæring",
            enhetsnr = ""
        ),
        MappeDto(
            id = mappeIdTilsynskrevende,
            navn = "EF Sak - 60 Særlig tilsynskrevende",
            enhetsnr = "4489"
        ),
        MappeDto(
            id = mappeIdSelvstendig,
            navn = "EF Sak - 61 Selvstendig næringsdrivende",
            enhetsnr = "4489"
        ),
        MappeDto(
            id = mappeIdUplassert,
            navn = "EF Sak 01 Uplassert",
            enhetsnr = ""
        )
    )

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

    private val journalpostBarnetilsyn =
        Journalpost(
            journalpostId = "111111111",
            journalposttype = Journalposttype.I,
            journalstatus = Journalstatus.MOTTATT,
            tema = "ENF",
            behandlingstema = BEHANDLINGSTEMA_BARNETILSYN,
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
                    brevkode = DokumentBrevkode.BARNETILSYN.verdi,
                    dokumentvarianter = listOf(Dokumentvariant(variantformat = Dokumentvariantformat.ARKIV))
                )
            )
        )

    private val journalpostSkolepenger =
        Journalpost(
            journalpostId = "111111111",
            journalposttype = Journalposttype.I,
            journalstatus = Journalstatus.MOTTATT,
            tema = "ENF",
            behandlingstema = BEHANDLINGSTEMA_SKOLEPENGER,
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
                    brevkode = DokumentBrevkode.SKOLEPENGER.verdi,
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
        behandlingstema: String?,
        behandlesAvApplikasjon: BehandlesAvApplikasjon
    ) =
        Oppgave(
            id = 123L,
            behandlingstema = behandlingstema,
            status = StatusEnum.OPPRETTET,
            tildeltEnhetsnr = "4489",
            behandlesAvApplikasjon = behandlesAvApplikasjon.applikasjon
        )
}

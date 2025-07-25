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
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.IOTestUtil
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.Testdata.utdanning
import no.nav.familie.http.client.RessursException
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.ef.søknad.Aktivitet
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
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
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.StatusEnum
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClientResponseException
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.test.assertEquals

internal class OppgaveServiceTest {
    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val opprettOppgaveMapper = spyk(OpprettOppgaveMapper())
    private val ettersendingService = mockk<EttersendingService>()
    private val cacheManager = ConcurrentMapCacheManager()

    private val oppgaveService: OppgaveService =
        OppgaveService(
            integrasjonerClient = integrasjonerClient,
            søknadService = søknadService,
            opprettOppgaveMapper = opprettOppgaveMapper,
            ettersendingService = ettersendingService,
            mappeService = MappeService(integrasjonerClient, søknadService, cacheManager),
        )

    @BeforeEach
    fun setUp() {
        every { integrasjonerClient.hentIdentForAktørId(any()) } returns Testdata.randomFnr()
        every { integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(any()) } returns
            listOf(
                Enhet(
                    enhetId = "4489",
                    enhetNavn = "NAY",
                ),
            )
        every { integrasjonerClient.lagOppgave(any()) } returns OppgaveResponse(oppgaveId = 1)
        every { integrasjonerClient.finnMappe(any()) } returns
            FinnMappeResponseDto(
                antallTreffTotalt = 1,
                mapper =
                    listOf(
                        MappeDto(
                            id = 123,
                            navn = "Uplassert",
                            enhetsnr = "",
                        ),
                        MappeDto(
                            id = 456,
                            navn = "65 Opplæring",
                            enhetsnr = "",
                        ),
                    ),
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
                    null,
                ),
            )
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0L, emptyList())
        every {
            søknadService.hentSøknad("123")
        } returns
            Søknad(
                søknadJson = EncryptedString("{}"),
                dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                journalpostId = "999",
                fnr = Testdata.randomFnr(),
                json = "{}",
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
                BehandlesAvApplikasjon.EF_SAK,
                "4489",
                OppgavePrioritet.NORM,
            )

        every {
            integrasjonerClient.lagOppgave(opprettOppgaveRequest)
        } throws lagRessursException()

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse = oppgaveService.lagJournalføringsoppgave(journalpostOvergangsstøand)

        assertEquals(1, oppgaveResponse)
    }

    private fun lagRessursException(): RessursException {
        val httpServerErrorException =
            HttpServerErrorException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Server error",
                IOTestUtil.readFile("opprett_oppgave_feilet.json").toByteArray(),
                Charset.defaultCharset(),
            )
        return lesRessurs(httpServerErrorException)?.let { RessursException(it, httpServerErrorException) }!!
    }

    private fun lesRessurs(e: RestClientResponseException): Ressurs<Any>? =
        try {
            if (e.responseBodyAsString.contains("status")) {
                objectMapper.readValue<Ressurs<Any>>(e.responseBodyAsString)
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }

    @Nested
    inner class LagJournalføringsoppgaveForSøknadId {
        @Test
        fun `skal opprette en ny oppgave for søknad`() {
            val søknadId = "enSøknadId"
            val journalpostId = "999"
            every { søknadService.hentSøknad(søknadId) } returns
                Søknad(
                    søknadJson = EncryptedString("{}"),
                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                    journalpostId = journalpostId,
                    fnr = Testdata.randomFnr(),
                    behandleINySaksbehandling = true,
                    json = "{}",
                )
            every {
                søknadService.hentSøknad(any())
            } returns
                Søknad(
                    søknadJson = EncryptedString(objectMapper.writeValueAsString(Testdata.søknadOvergangsstønad)),
                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                    journalpostId = "1234",
                    fnr = Testdata.randomFnr(),
                    behandleINySaksbehandling = true,
                    json = "{}",
                )
            every { integrasjonerClient.hentJournalpost(any()) } returns journalpostOvergangsstøand
            every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpostOvergangsstøand
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())

            oppgaveService.lagJournalføringsoppgaveForSøknadId(søknadId)

            verify {
                opprettOppgaveMapper.toJournalføringsoppgave(
                    any(),
                    BehandlesAvApplikasjon.EF_SAK,
                    "4489",
                    any(),
                )
            }
        }
    }

    @Nested
    inner class LagJournalføringsoppgaveForJournalpostId {
        @Test
        fun `skal opprette en ny journalføringsoppgave`() {
            val journalpostId = UUID.randomUUID().toString()
            val journalpost =
                journalpostOvergangsstøand.copy(
                    bruker = Bruker("1", type = BrukerIdType.FNR),
                    journalpostId = journalpostId,
                )

            every { integrasjonerClient.hentJournalpost(journalpostId) } returns journalpost
            every { integrasjonerClient.finnOppgaver(journalpostId, any()) } returns
                FinnOppgaveResponseDto(
                    0,
                    emptyList(),
                )

            oppgaveService.lagJournalføringsoppgaveForJournalpostId(journalpostId)

            verify {
                opprettOppgaveMapper.toJournalføringsoppgave(
                    any(),
                    BehandlesAvApplikasjon.EF_SAK,
                    "4489",
                    OppgavePrioritet.NORM,
                )
            }
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

            verify {
                opprettOppgaveMapper.toJournalføringsoppgave(
                    any(),
                    BehandlesAvApplikasjon.EF_SAK,
                    "4489",
                    OppgavePrioritet.NORM,
                )
            }
        }
    }

    @Nested
    inner class Prioritet {
        @Test
        fun `sett høy prioritet pga sommertid`() {
            val soknadId = "123"
            val opprettOppgaveSlot = slot<OpprettOppgaveRequest>()
            val sommertid = LocalDate.of(LocalDateTime.now().year, 7, 20)

            every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
            every { søknadService.hentSøknad(soknadId) } returns
                SøknadMapper
                    .fromDto(
                        Testdata.søknadOvergangsstønad.copy(
                            aktivitet =
                                Søknadsfelt(
                                    "aktivitet",
                                    tomAktivitet.copy(underUtdanning = Søknadsfelt("", utdanning())),
                                ),
                        ),
                        true,
                    ).copy(opprettetTid = LocalDateTime.of(sommertid, LocalTime.now()), journalpostId = "11111111111")
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            every { integrasjonerClient.lagOppgave(capture(opprettOppgaveSlot)) } returns OppgaveResponse(1)

            oppgaveService.lagJournalføringsoppgaveForSøknadId(soknadId)

            assertThat(opprettOppgaveSlot.captured.prioritet).isEqualTo(OppgavePrioritet.HOY)
        }

        @Test
        fun `ikke sett høy prioritet utenfor sommertid`() {
            val opprettOppgaveSlot = slot<OpprettOppgaveRequest>()
            val soknadId = "123"
            val utenforSommertid = LocalDate.of(LocalDateTime.now().year, 5, 20)

            every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
            every { søknadService.hentSøknad(soknadId) } returns
                SøknadMapper
                    .fromDto(
                        Testdata.søknadOvergangsstønad.copy(
                            aktivitet =
                                Søknadsfelt(
                                    "aktivitet",
                                    tomAktivitet.copy(underUtdanning = Søknadsfelt("", utdanning())),
                                ),
                        ),
                        true,
                    ).copy(opprettetTid = LocalDateTime.of(utenforSommertid, LocalTime.now()), journalpostId = "11111111111")
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            every { integrasjonerClient.lagOppgave(capture(opprettOppgaveSlot)) } returns OppgaveResponse(1)

            oppgaveService.lagJournalføringsoppgaveForSøknadId(soknadId)

            assertThat(opprettOppgaveSlot.captured.prioritet).isEqualTo(OppgavePrioritet.NORM)
        }

        @Test
        fun `ikke sett høy prioritet i sommertid hvis aktivitet ikke er under utdanning`() {
            val opprettOppgaveSlot = slot<OpprettOppgaveRequest>()
            val soknadId = "123"
            val utenforSommertid = LocalDate.of(LocalDateTime.now().year, 7, 20)

            every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
            every { søknadService.hentSøknad(soknadId) } returns
                SøknadMapper
                    .fromDto(
                        Testdata.søknadOvergangsstønad.copy(
                            aktivitet =
                                Søknadsfelt(
                                    "aktivitet",
                                    tomAktivitet,
                                ),
                        ),
                        true,
                    ).copy(opprettetTid = LocalDateTime.of(utenforSommertid, LocalTime.now()), journalpostId = "11111111111")
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            every { integrasjonerClient.lagOppgave(capture(opprettOppgaveSlot)) } returns OppgaveResponse(1)

            oppgaveService.lagJournalføringsoppgaveForSøknadId(soknadId)

            assertThat(opprettOppgaveSlot.captured.prioritet).isEqualTo(OppgavePrioritet.NORM)
        }

        @Test
        fun `ikke sett høy prioritet i sommertid hvis søknad ikke er overgangsstønad`() {
            val opprettOppgaveSlot = slot<OpprettOppgaveRequest>()
            val soknadId = "123"
            val utenforSommertid = LocalDate.of(LocalDateTime.now().year, 7, 20)

            every { integrasjonerClient.hentJournalpost(any()) } returns journalpost
            every { søknadService.hentSøknad(soknadId) } returns
                SøknadMapper
                    .fromDto(
                        Testdata.søknadBarnetilsyn.copy(
                            aktivitet =
                                Søknadsfelt(
                                    "aktivitet",
                                    tomAktivitet,
                                ),
                        ),
                        true,
                    ).copy(opprettetTid = LocalDateTime.of(utenforSommertid, LocalTime.now()), journalpostId = "11111111111")
            every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0, emptyList())
            every { integrasjonerClient.lagOppgave(capture(opprettOppgaveSlot)) } returns OppgaveResponse(1)

            oppgaveService.lagJournalføringsoppgaveForSøknadId(soknadId)

            assertThat(opprettOppgaveSlot.captured.prioritet).isEqualTo(OppgavePrioritet.NORM)
        }

        private val journalpost =
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
                null,
            )
    }

    @Nested
    inner class OppdaterOppgaveMedRiktigMappeId {
        @Test
        fun `skal være uplassert hvis sak kan behandles i ny løsning for barnetilsyn`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Barnetilsyn,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )

            every { søknadService.hentSøknad(any()) } returns søknadBarnetilsyn()

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "1")

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(any(), any()) }
        }

        @Test
        fun `skal flytte oppgave til mappe for selvstendige hvis søknad har firma`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdSelvstendig = 1

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper = lagMapper(gammelMappeIdSelvstendig = gammelMappeIdSelvstendig),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Overgangsstønad,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad("123") } returns søknadOvergangsstønad(erSelvstendig = true)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(gammelMappeIdSelvstendig.toLong())
        }

        @Test
        fun `skal flytte oppgave til mappe for særlig tilsynskrevende barn for overgangsstønad`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdTilsynskrevende = 3456

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper =
                        lagMapper(
                            gammelMappeIdTilsynskrevende = gammelMappeIdTilsynskrevende,
                        ),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Overgangsstønad,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad("123") } returns
                søknadOvergangsstønad(
                    erSelvstendig = true,
                    harTilsynskrevendeBarn = true,
                )
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(gammelMappeIdTilsynskrevende.toLong())
        }

        @Test
        fun `skal flytte oppgave til mappe for særlig tilsynskrevende barn for barnetilsyn`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdTilsynskrevende = 3456

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper =
                        lagMapper(
                            gammelMappeIdTilsynskrevende = gammelMappeIdTilsynskrevende,
                        ),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Barnetilsyn,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad("123") } returns
                søknadBarnetilsyn(
                    erSelvstendig = true,
                    harTilsynskrevendeBarn = true,
                )
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(gammelMappeIdTilsynskrevende.toLong())
        }

        @Test
        fun `skal flytte barnetilsyn-oppgave til mappe for selvstendig`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdSelvstendig = 1

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper =
                        lagMapper(
                            gammelMappeIdSelvstendig = gammelMappeIdSelvstendig,
                        ),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Barnetilsyn,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad("123") } returns
                søknadBarnetilsyn(
                    erSelvstendig = true,
                    harTilsynskrevendeBarn = false,
                )
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(gammelMappeIdSelvstendig.toLong())
        }

        @Test
        fun `les inn komplett barnetilsyn-søknad, oppgave til mappe for særlig tilsynskrevende`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdTilsynskrevende = 654

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper = lagMapper(gammelMappeIdTilsynskrevende = 654),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Barnetilsyn,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            val søknadBarnetilsyn =
                objectMapper.readValue<SøknadBarnetilsyn>(IOTestUtil.readFile("barnetilsyn_særlige_tilsynsbehov_soknad.json"))

            every { søknadService.hentSøknad("123") } returns SøknadMapper.fromDto(søknadBarnetilsyn, true)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            assertThat(oppgaveSlot.captured.mappeId).isEqualTo(gammelMappeIdTilsynskrevende.toLong())
        }

        @Test
        fun `skal la være å oppdatere oppgave hvis vanlig søknad `() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdUplassert = 1

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper = lagMapper(gammelMappeIdUplassert = gammelMappeIdUplassert),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Overgangsstønad,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad("123") } returns søknadOvergangsstønad(false)
            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "123")

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(any(), any()) }
        }

        @Test
        fun `skal være en uplassert oppgave hvis skolepenger og skal behandles i ny løsning`() {
            val oppgaveId: Long = 123
            val gammelMappeIdOpplæring = 1

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper =
                        listOf(
                            MappeDto(
                                id = gammelMappeIdOpplæring,
                                navn = "65 Opplæring",
                                enhetsnr = "",
                            ),
                        ),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Skolepenger,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { søknadService.hentSøknad(any()) } returns søknadSkolepenger()

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "-1")

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal ikke flytte oppgave til mappe hvis behandlingstema er null (arbeidssøkerskjema)`() {
            val oppgaveId: Long = 123

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = null,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "-1")

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(oppgaveId, any()) }
        }

        @Test
        fun `skal være uplassert hvis barnetilsyn og skal behandles i ny løsning`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()

            val gammelMappeIdUplassert = 123
            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper =
                        listOf(
                            MappeDto(
                                id = gammelMappeIdUplassert,
                                navn = "Uplassert",
                                enhetsnr = "",
                            ),
                        ),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Barnetilsyn,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad(any()) } returns søknadBarnetilsyn()

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "-1")

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(any(), any()) }
        }

        @Test
        fun `skal ikke sette mappeId hvis overgangsstønad`() {
            val oppgaveId: Long = 123
            val oppgaveSlot = slot<Oppgave>()
            val gammelMappeIdUplassert = 123

            every { integrasjonerClient.finnMappe(any()) } returns
                FinnMappeResponseDto(
                    antallTreffTotalt = 1,
                    mapper =
                        listOf(
                            MappeDto(
                                id = gammelMappeIdUplassert,
                                navn = "Uplassert",
                                enhetsnr = "",
                            ),
                        ),
                )

            every { integrasjonerClient.hentOppgave(oppgaveId) } returns
                lagOppgaveForFordeling(
                    behandlingstema = Behandlingstema.Overgangsstønad,
                    behandlesAvApplikasjon = BehandlesAvApplikasjon.EF_SAK,
                )
            every { integrasjonerClient.oppdaterOppgave(oppgaveId, capture(oppgaveSlot)) } returns 123
            every { søknadService.hentSøknad(any()) } returns søknadOvergangsstønad()

            oppgaveService.oppdaterOppgaveMedRiktigMappeId(oppgaveId, "-1")

            verify(exactly = 0) { integrasjonerClient.oppdaterOppgave(any(), any()) }
        }

        private fun søknadOvergangsstønad(
            erSelvstendig: Boolean = false,
            harTilsynskrevendeBarn: Boolean = false,
        ): Søknad {
            val startSøknadMedAlt = Testdata.søknadOvergangsstønad

            val situasjon =
                when (harTilsynskrevendeBarn) {
                    false -> startSøknadMedAlt.situasjon.verdi.copy(barnMedSærligeBehov = null)
                    true -> startSøknadMedAlt.situasjon.verdi
                }

            val aktivitet =
                when (erSelvstendig) {
                    true -> startSøknadMedAlt.aktivitet
                    false -> Søknadsfelt("aktivitet", tomAktivitet)
                }

            val søknadOvergangsstønad =
                startSøknadMedAlt.copy(
                    aktivitet = aktivitet,
                    situasjon = Søknadsfelt("s", situasjon),
                )

            val søknad =
                SøknadMedVedlegg(
                    søknad = søknadOvergangsstønad,
                    vedlegg = listOf(),
                    dokumentasjonsbehov = listOf(),
                    behandleINySaksbehandling = true,
                )
            return SøknadMapper.fromDto(søknad.søknad, true)
        }
    }

    private fun søknadBarnetilsyn(
        erSelvstendig: Boolean = false,
        harTilsynskrevendeBarn: Boolean = false,
    ): Søknad {
        val startSøknadMedAlt = Testdata.søknadBarnetilsyn

        val særligTilsynskrevende =
            startSøknadMedAlt.barn.verdi.first().barnepass?.verdi?.copy(
                årsakBarnepass =
                    Søknadsfelt(
                        "årsak",
                        "årsak",
                        svarId = "trengerMerPassEnnJevnaldrede",
                    ),
            )!!

        val barn =
            when (harTilsynskrevendeBarn) {
                true ->
                    startSøknadMedAlt.barn.verdi
                        .first()
                        .copy(barnepass = Søknadsfelt("barnepass", særligTilsynskrevende, svarId = særligTilsynskrevende))

                false -> startSøknadMedAlt.barn.verdi.first()
            }

        val aktivitet =
            when (erSelvstendig) {
                true -> startSøknadMedAlt.aktivitet
                false -> Søknadsfelt("aktivitet", tomAktivitet)
            }

        val søknadBarnetilsyn =
            startSøknadMedAlt.copy(
                aktivitet = aktivitet,
                barn = Søknadsfelt("Barn", listOf(barn)),
            )

        return SøknadMapper.fromDto(søknadBarnetilsyn, true)
    }

    private fun søknadSkolepenger(): Søknad = SøknadMapper.fromDto(Testdata.søknadSkolepenger, true)

    val tomAktivitet =
        Aktivitet(
            hvordanErArbeidssituasjonen =
                Søknadsfelt(
                    "Hvordan er arbeidssituasjonen din?",
                    listOf(
                        "Jeg er hjemme med barn under 1 år",
                        "Jeg er i arbeid",
                        "Jeg er selvstendig næringsdrivende eller frilanser",
                    ),
                ),
            arbeidsforhold = null,
            selvstendig = null,
            firmaer = null,
            virksomhet = null,
            arbeidssøker = null,
            underUtdanning = null,
            aksjeselskap = null,
            erIArbeid = null,
            erIArbeidDokumentasjon = null,
        )

    private fun lagMapper(
        mappeIdSelvstendig: Int = 456,
        mappeIdUplassert: Int = 123,
        mappeIdTilsynskrevende: Int = 765,
        gammelMappeIdSelvstendig: Int = 100,
        gammelMappeIdUplassert: Int = 102,
        gammelMappeIdTilsynskrevende: Int = 103,
    ) = listOf(
        MappeDto(
            id = 987,
            navn = "EF Sak - 65 Opplæring",
            enhetsnr = "",
        ),
        MappeDto(
            id = mappeIdTilsynskrevende,
            navn = "EF Sak - 60 Særlig tilsynskrevende",
            enhetsnr = "4489",
        ),
        MappeDto(
            id = mappeIdSelvstendig,
            navn = "EF Sak - 61 Selvstendig næringsdrivende",
            enhetsnr = "4489",
        ),
        MappeDto(
            id = mappeIdUplassert,
            navn = "EF Sak 01 Uplassert",
            enhetsnr = "",
        ),
        MappeDto(
            id = 99,
            navn = "65 Opplæring",
            enhetsnr = "",
        ),
        MappeDto(
            id = gammelMappeIdTilsynskrevende,
            navn = "60 Særlig tilsynskrevende",
            enhetsnr = "4489",
        ),
        MappeDto(
            id = gammelMappeIdSelvstendig,
            navn = "61 Selvstendig næringsdrivende",
            enhetsnr = "4489",
        ),
        MappeDto(
            id = gammelMappeIdUplassert,
            navn = "Uplassert",
            enhetsnr = "",
        ),
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
                        dokumentvarianter =
                            listOf(
                                Dokumentvariant(variantformat = Dokumentvariantformat.ARKIV, saksbehandlerHarTilgang = true),
                            ),
                    ),
                ),
        )

    private val ettersending =
        Ettersending(
            id = UUID.randomUUID(),
            ettersendingJson = EncryptedString(""),
            ettersendingPdf = EncryptedFile("abc".toByteArray()),
            stønadType = "OVERGANGSSTØNAD",
            journalpostId = "123Abc",
            fnr = "12345678901",
            taskOpprettet = true,
            opprettetTid = LocalDateTime.of(2021, 5, 1, 13, 2),
        )

    private fun lagOppgaveForFordeling(
        behandlingstema: Behandlingstema?,
        behandlesAvApplikasjon: BehandlesAvApplikasjon,
    ) = Oppgave(
        id = 123L,
        behandlingstema = behandlingstema?.value,
        status = StatusEnum.OPPRETTET,
        tildeltEnhetsnr = "4489",
        behandlesAvApplikasjon = behandlesAvApplikasjon.applikasjon,
    )
}

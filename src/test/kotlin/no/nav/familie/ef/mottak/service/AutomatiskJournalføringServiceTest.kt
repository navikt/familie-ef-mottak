package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringRequest
import no.nav.familie.kontrakter.ef.journalføring.AutomatiskJournalføringResponse
import no.nav.familie.kontrakter.ef.søknad.Aktivitet
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AutomatiskJournalføringServiceTest {
    private val saksbehandlingClient = mockk<SaksbehandlingClient>()
    private val søknad = SøknadMapper.fromDto(Testdata.søknadOvergangsstønad, true)

    private val automatiskJournalføringService =
        AutomatiskJournalføringService(
            taskService = mockk(),
            saksbehandlingClient = saksbehandlingClient,
        )

    private val automatiskJournalføringResponse =
        AutomatiskJournalføringResponse(
            fagsakId = UUID.randomUUID(),
            behandlingId = UUID.randomUUID(),
        )

    private val mappeId = 1L

    @Test
    internal fun `Skal returnere true når journalføring i ef-sak går bra `() {
        every {
            saksbehandlingClient.journalførAutomatisk(any())
        } returns automatiskJournalføringResponse

        assertTrue {
            automatiskJournalføringService.journalførAutomatisk(
                personIdent = "",
                journalpostId = "",
                stønadstype = StønadType.OVERGANGSSTØNAD,
                mappeId = mappeId,
                søknad,
            )
        }
    }

    @Test
    internal fun `Skal returnere false når journalføring i ef-sak feiler `() {
        every {
            saksbehandlingClient.journalførAutomatisk(any())
        } throws RuntimeException("Feil")

        assertFalse {
            automatiskJournalføringService.journalførAutomatisk(
                personIdent = "",
                journalpostId = "",
                stønadstype = StønadType.OVERGANGSSTØNAD,
                mappeId = mappeId,
                søknad,
            )
        }
    }

    @Nested
    inner class Prioritet {
        @Test
        internal fun `skal sette høy prioritet når søknad har aktivitet under utdanning i sommerperiode`() {
            val automatiskJournalføringRequestSlot = slot<AutomatiskJournalføringRequest>()
            val sommertid = LocalDate.of(LocalDateTime.now().year, 7, 20)
            val soknad =
                SøknadMapper
                    .fromDto(
                        Testdata.søknadOvergangsstønad.copy(
                            aktivitet =
                                Søknadsfelt(
                                    "aktivitet",
                                    tomAktivitet.copy(underUtdanning = Søknadsfelt("", Testdata.utdanning())),
                                ),
                        ),
                        true,
                    ).copy(opprettetTid = LocalDateTime.of(sommertid, LocalTime.now()), journalpostId = "11111111111")

            every { saksbehandlingClient.journalførAutomatisk(capture(automatiskJournalføringRequestSlot)) } returns
                AutomatiskJournalføringResponse(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                )

            automatiskJournalføringService.journalførAutomatisk(
                personIdent = "",
                journalpostId = "11111111111",
                stønadstype = StønadType.OVERGANGSSTØNAD,
                mappeId = mappeId,
                soknad,
            )

            Assertions.assertThat(automatiskJournalføringRequestSlot.captured.prioritet).isEqualTo(OppgavePrioritet.HOY)
        }

        @Test
        internal fun `skal ikke sette høy prioritet når søknad har aktivitet utenfor sommerperiode`() {
            val automatiskJournalføringRequestSlot = slot<AutomatiskJournalføringRequest>()
            val sommertid = LocalDate.of(LocalDateTime.now().year, 2, 20)
            val soknad =
                SøknadMapper
                    .fromDto(
                        Testdata.søknadOvergangsstønad.copy(
                            aktivitet =
                                Søknadsfelt(
                                    "aktivitet",
                                    tomAktivitet.copy(underUtdanning = Søknadsfelt("", Testdata.utdanning())),
                                ),
                        ),
                        true,
                    ).copy(opprettetTid = LocalDateTime.of(sommertid, LocalTime.now()), journalpostId = "11111111111")

            every { saksbehandlingClient.journalførAutomatisk(capture(automatiskJournalføringRequestSlot)) } returns
                AutomatiskJournalføringResponse(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                )

            automatiskJournalføringService.journalførAutomatisk(
                personIdent = "",
                journalpostId = "11111111111",
                stønadstype = StønadType.OVERGANGSSTØNAD,
                mappeId = mappeId,
                soknad,
            )
            Assertions.assertThat(automatiskJournalføringRequestSlot.captured.prioritet).isEqualTo(OppgavePrioritet.NORM)
        }

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
    }
}

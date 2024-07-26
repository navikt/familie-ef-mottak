package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

internal class SøknadRepositoryTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var søknadRepository: SøknadRepository

    @Test
    internal fun `findFirstByTaskOpprettetIsFalse returnerer én soknad med taskOpprettet false`() {
        søknadRepository.insert(søknad())
        søknadRepository.insert(søknad())
        søknadRepository.insert(søknad(taskOpprettet = true))

        val soknadUtenTask = søknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask?.taskOpprettet).isFalse
    }

    @Test
    internal fun `findFirstByTaskOpprettetIsFalse takler null result`() {
        søknadRepository.insert(søknad(taskOpprettet = true))

        val soknadUtenTask = søknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask).isNull()
    }

    @Test
    internal fun `findByJournalpostId skal returnere søknad`() {
        søknadRepository.insert(søknad(taskOpprettet = true, journalpostId = "123"))

        val soknadUtenTask = søknadRepository.findByJournalpostId("123")
        assertThat(soknadUtenTask).isNotNull
    }

    @Test
    internal fun `findByJournalpostId skal returnere null`() {
        søknadRepository.insert(søknad(taskOpprettet = true))
        val soknadUtenTask = søknadRepository.findByJournalpostId("123")
        assertThat(soknadUtenTask).isNull()
    }

    @Test
    internal fun `findAllByFnr returnerer flere søknader`() {
        val ident = "12345678"

        val søknad =
            Søknad(
                søknadJson = EncryptedString("kåre"),
                fnr = ident,
                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                taskOpprettet = true,
                opprettetTid = LocalDateTime.now().minusDays(1),
            )
        val søknad2 =
            Søknad(
                søknadJson = EncryptedString("kåre"),
                fnr = ident,
                dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                taskOpprettet = true,
            )
        søknadRepository.insert(søknad)
        val søknadOvergangsstønad = søknadRepository.insert(søknad2)

        val søknadBarnetilsyn =
            søknadRepository.insert(
                Søknad(
                    søknadJson = EncryptedString("kåre"),
                    fnr = ident,
                    dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                    taskOpprettet = true,
                ),
            )

        val søknader = søknadRepository.finnSisteSøknadenPerStønadtype(ident)

        assertThat(søknader)
            .usingRecursiveComparison()
            .ignoringFields("opprettetTid")
            .ignoringCollectionOrder()
            .isEqualTo(listOf(søknadOvergangsstønad, søknadBarnetilsyn))
    }

    @Test
    internal fun `finnSøknaderKlarTilReduksjon finner journalførte søknader eldre enn 3 måneder med søknadPdf`() {
        val ikkeJournalFørtEldreEnn3måneder = søknadRepository.insert(søknad(opprettetTid = LocalDateTime.now().minusMonths(4)))
        val journalFørtEldreEnn3MånederUtenPdf =
            søknadRepository.insert(
                søknad(
                    opprettetTid = LocalDateTime.now().minusMonths(4),
                    journalpostId = "321321",
                ),
            )
        val journalFørtEldreEnn3MånederMedPdf =
            søknadRepository.insert(
                søknad(
                    opprettetTid = LocalDateTime.now().minusMonths(4),
                    journalpostId = "321321",
                    søknadPdf = EncryptedFile(ByteArray(5)),
                ),
            )
        val journalFørtYngreEnn3MånederMedPdf =
            søknadRepository.insert(
                søknad(
                    opprettetTid = LocalDateTime.now().minusMonths(2),
                    journalpostId = "321321",
                    søknadPdf = EncryptedFile(ByteArray(5)),
                ),
            )

        val søknader = søknadRepository.finnSøknaderKlarTilReduksjon(LocalDateTime.now().minusMonths(3))

        assertThat(søknader).size().isEqualTo(1)
        assertThat(søknader).contains(journalFørtEldreEnn3MånederMedPdf.id)
        assertThat(søknader).doesNotContain(
            ikkeJournalFørtEldreEnn3måneder.id,
            journalFørtEldreEnn3MånederUtenPdf.id,
            journalFørtYngreEnn3MånederMedPdf.id,
        )
    }

    @Test
    internal fun `finnSøknaderKlarTilSletting finner journalførte søknader eldre enn 3 måneder uten søknadPdf`() {
        val ikkeJournalFørtEldreEnn3måneder = søknadRepository.insert(søknad(opprettetTid = LocalDateTime.now().minusMonths(4)))
        val journalFørtEldreEnn3MånederUtenPdf =
            søknadRepository.insert(
                søknad(
                    opprettetTid = LocalDateTime.now().minusMonths(4),
                    journalpostId = "321321",
                ),
            )
        val journalFørtEldreEnn3MånederMedPdf =
            søknadRepository.insert(
                søknad(
                    opprettetTid = LocalDateTime.now().minusMonths(4),
                    journalpostId = "321321",
                    søknadPdf = EncryptedFile(ByteArray(5)),
                ),
            )
        val journalFørtYngreEnn3MånederMedPdf =
            søknadRepository.insert(
                søknad(
                    opprettetTid = LocalDateTime.now().minusMonths(2),
                    journalpostId = "321321",
                    søknadPdf = EncryptedFile(ByteArray(5)),
                ),
            )

        val søknader = søknadRepository.finnSøknaderKlarTilSletting(LocalDateTime.now().minusMonths(3))

        assertThat(søknader).size().isEqualTo(1)
        assertThat(søknader).contains(journalFørtEldreEnn3MånederUtenPdf.id)
        assertThat(søknader).doesNotContain(
            ikkeJournalFørtEldreEnn3måneder.id,
            journalFørtEldreEnn3MånederMedPdf.id,
            journalFørtYngreEnn3MånederMedPdf.id,
        )
    }
}

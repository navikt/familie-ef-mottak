package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class SøknadRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var søknadRepository: SøknadRepository


    @Test
    internal fun `findFirstByTaskOpprettetIsFalse returnerer én soknad med taskOpprettet false`() {

        søknadRepository.save(søknad())
        søknadRepository.save(søknad())
        søknadRepository.save(søknad(taskOpprettet = true))

        val soknadUtenTask = søknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask?.taskOpprettet).isFalse
    }

    @Test
    internal fun `findFirstByTaskOpprettetIsFalse takler null result`() {
        søknadRepository.save(søknad(taskOpprettet = true))

        val soknadUtenTask = søknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask).isNull()
    }

    @Test
    internal fun `findByJournalpostId skal returnere søknad`() {
        søknadRepository.save(søknad(taskOpprettet = true, journalpostId = "123"))

        val soknadUtenTask = søknadRepository.findByJournalpostId("123")
        assertThat(soknadUtenTask).isNotNull
    }

    @Test
    internal fun `findByJournalpostId skal returnere null`() {
        søknadRepository.save(søknad(taskOpprettet = true))
        val soknadUtenTask = søknadRepository.findByJournalpostId("123")
        assertThat(soknadUtenTask).isNull()
    }

    @Test
    internal fun `findAllByFnr returnerer flere søknader`() {
        val ident = "12345678"

        val søknadOvergangsstønad = søknadRepository.save(Søknad(søknadJson = "kåre",
                                                                 fnr = ident,
                                                                 dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                                                 taskOpprettet = true))

        val søknadBarnetilsyn = søknadRepository.save(Søknad(søknadJson = "kåre",
                                                             fnr = ident,
                                                             dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                                                             taskOpprettet = true))

        val søknader = søknadRepository.findAllByFnr(ident)

        assertThat(søknader).usingRecursiveComparison()
                .ignoringFields("opprettetTid")
                .ignoringCollectionOrder()
                .isEqualTo(listOf(søknadOvergangsstønad, søknadBarnetilsyn))
    }
}

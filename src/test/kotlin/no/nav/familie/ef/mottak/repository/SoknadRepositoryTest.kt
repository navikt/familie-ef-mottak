package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class SoknadRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var soknadRepository: SoknadRepository


    @Test
    internal fun `findFirstByTaskOpprettetIsFalse returnerer én soknad med taskOpprettet false`() {

        soknadRepository.save(Soknad(søknadJson = "bob",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD))
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD))
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     taskOpprettet = true))

        val soknadUtenTask = soknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask?.taskOpprettet).isFalse
    }

    @Test
    internal fun `findFirstByTaskOpprettetIsFalse takler null result`() {
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     taskOpprettet = true))

        val soknadUtenTask = soknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask).isNull()
    }

    @Test
    internal fun `findByJournalpostId skal returnere søknad`() {
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     taskOpprettet = true,
                                     journalpostId = "123"))
        val soknadUtenTask = soknadRepository.findByJournalpostId("123")
        assertThat(soknadUtenTask).isNotNull
    }

    @Test
    internal fun `findByJournalpostId skal returnere null`() {
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     taskOpprettet = true))
        val soknadUtenTask = soknadRepository.findByJournalpostId("123")
        assertThat(soknadUtenTask).isNull()
    }

}

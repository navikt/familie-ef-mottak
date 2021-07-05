package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
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
    internal fun `findAllByFnr returnerer flere søknader`() {
        val ident = "12345678"

        val søknadOvergangsstønad = soknadRepository.save(Soknad(søknadJson = "kåre",
                                                                 fnr = ident,
                                                                 dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                                                 taskOpprettet = true))

        val søknadBarnetilsyn = soknadRepository.save(Soknad(søknadJson = "kåre",
                                                             fnr = ident,
                                                             dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                                                             taskOpprettet = true))

        val søknader = soknadRepository.findAllByFnr(ident)

        assertThat(søknader).usingElementComparatorIgnoringFields("opprettetTid").containsExactlyInAnyOrder(søknadOvergangsstønad, søknadBarnetilsyn)
    }

}

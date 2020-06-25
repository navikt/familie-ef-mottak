package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.task.LagPdfTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("local")
internal class SoknadRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var soknadRepository: SoknadRepository

    @Test
    internal fun `findFirstByTaskOpprettetIsFalse returnerer én soknad med taskOpprettet false`() {

        soknadRepository.save(Soknad(søknadJson = "bob",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     vedlegg = null))
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     vedlegg = null))
        soknadRepository.save(Soknad(søknadJson = "kåre",
                                     fnr = "ded",
                                     dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                                     vedlegg = null,
                                     taskOpprettet = true))

        val soknadUtenTask = soknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask?.taskOpprettet).isFalse()
    }

    @Test
    internal fun `findFirstByTaskOpprettetIsFalse takler null result`() {
        val soknadUtenTask = soknadRepository.findFirstByTaskOpprettetIsFalse()

        assertThat(soknadUtenTask).isNull()
    }

}

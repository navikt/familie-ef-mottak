package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@ActiveProfiles("local")
internal class HendelsesloggRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var hendelsesloggRepository: HendelsesloggRepository

    @Test
    internal fun `skal hente max av kafka_offset`() {
        hendelsesloggRepository.save(Hendelseslogg(50, UUID.randomUUID().toString()))
        hendelsesloggRepository.save(Hendelseslogg(200, UUID.randomUUID().toString()))
        hendelsesloggRepository.save(Hendelseslogg(100, UUID.randomUUID().toString()))
        assertThat(hendelsesloggRepository.hentMaxOffset()).isEqualTo(200)
    }

    @Test
    internal fun `hvis det ikke finnes offset i databasen fra tidligere har noe gått veldig galt`() {
        assertThatThrownBy { hendelsesloggRepository.hentMaxOffset() }
                .isInstanceOf(EmptyResultDataAccessException::class.java)
    }

}

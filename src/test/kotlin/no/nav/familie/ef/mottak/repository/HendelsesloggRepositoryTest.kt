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
        val hendelseId = UUID.randomUUID().toString()
        hendelsesloggRepository.save(Hendelseslogg(200, hendelseId))
        hendelsesloggRepository.save(Hendelseslogg(100, UUID.randomUUID().toString()))
        assertThat(hendelsesloggRepository.existsByHendelseId(hendelseId)).isTrue
    }
}

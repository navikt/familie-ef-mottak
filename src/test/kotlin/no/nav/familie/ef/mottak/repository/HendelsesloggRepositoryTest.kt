package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class HendelsesloggRepositoryTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var hendelsesloggRepository: HendelsesloggRepository

    @Test
    internal fun `skal hente max av kafka_offset`() {
        hendelsesloggRepository.insert(Hendelseslogg(50, UUID.randomUUID().toString()))
        val hendelseId = UUID.randomUUID().toString()
        hendelsesloggRepository.insert(Hendelseslogg(200, hendelseId))
        hendelsesloggRepository.insert(Hendelseslogg(100, UUID.randomUUID().toString()))
        assertThat(hendelsesloggRepository.existsByHendelseId(hendelseId)).isTrue
    }
}

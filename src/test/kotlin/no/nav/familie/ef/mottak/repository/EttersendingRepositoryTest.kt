package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.kontrakter.ef.ettersending.EttersendingDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class EttersendingRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var ettersendingRepository: EttersendingRepository


    @Test
    internal fun `lagre og hent ettersending`() {

        val ettersendingDto = EttersendingDto("12345678910",
                                              StønadType.OVERGANGSSTØNAD,
                                              null,
                                              null)

        val ettersending = ettersendingRepository.save(EttersendingMapper.fromDto(ettersendingDto))

        assertThat(ettersendingRepository.count()).isEqualTo(1)
        assertThat(objectMapper.readValue(ettersending.ettersendingJson, EttersendingDto::class.java)).usingRecursiveComparison()
                .isEqualTo(ettersendingDto)
    }

}

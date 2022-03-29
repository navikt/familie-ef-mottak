package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.kontrakter.ef.ettersending.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID

internal class EttersendingRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var ettersendingRepository: EttersendingRepository


    @Test
    internal fun `lagre og hent ettersending`() {

        val vedlegg = Vedlegg(UUID.randomUUID().toString(), "Vedlegg 2", "Vedleggtittel 2")
        val dokumentasjonsbehov = Dokumentasjonsbehov(id = UUID.randomUUID().toString(),
                                                      søknadsdata = null,
                                                      dokumenttype = "DOKUMENTASJON_IKKE_VILLIG_TIL_ARBEID",
                                                      beskrivelse = "Dokumentasjon på at du ikke kan ta arbeid",
                                                      stønadType = StønadType.OVERGANGSSTØNAD,
                                                      innsendingstidspunkt = null,
                                                      vedlegg = listOf(vedlegg))
        val personIdent = "123456789010"

        val ettersendelseDto = EttersendelseDto(
                listOf(dokumentasjonsbehov), personIdent = personIdent)
        val ettersending = ettersendingRepository.save(EttersendingMapper.fromDto(StønadType.OVERGANGSSTØNAD, ettersendelseDto))


        assertThat(ettersendingRepository.count()).isEqualTo(1)
        assertThat(objectMapper.readValue(ettersending.ettersendingJson, EttersendelseDto::class.java)).usingRecursiveComparison()
                .isEqualTo(ettersendelseDto)
    }

}

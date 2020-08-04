package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("local")
internal class VedleggRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var soknadRepository: SoknadRepository

    @Autowired lateinit var vedleggRepository: VedleggRepository

    @Test
    internal fun `findBySøknadId returnerer vedlegg`() {
        val søknadId = soknadRepository.save(Soknad(søknadJson = "bob",
                                                    fnr = "ded",
                                                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD)).id
        vedleggRepository.save(Vedlegg(UUID.randomUUID(), søknadId, "navn", "tittel1", Fil(byteArrayOf(12))))

        assertThat(vedleggRepository.findBySøknadId(søknadId)).hasSize(1)
        assertThat(vedleggRepository.findBySøknadId("finnes ikke")).isEmpty()
    }

    @Test
    internal fun `findTitlerBySøknadId returnerer titler`() {
        val søknadId = soknadRepository.save(Soknad(søknadJson = "bob",
                                                    fnr = "ded",
                                                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD)).id
        vedleggRepository.save(Vedlegg(UUID.randomUUID(), søknadId, "navn", "tittel1", Fil(byteArrayOf(12))))

        val vedlegg = vedleggRepository.findTitlerBySøknadId(søknadId)

        assertThat(vedlegg).hasSize(1)
    }

}

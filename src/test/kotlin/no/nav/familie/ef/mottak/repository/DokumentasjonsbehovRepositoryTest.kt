package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class DokumentasjonsbehovRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var soknadRepository: SoknadRepository

    @Autowired
    lateinit var dokumentasjonsbehovRepository: DokumentasjonsbehovRepository


    @Test
    internal fun `lagre og hent dokumentasjonsbehov`() {

        val søknad = soknadRepository.save(Soknad(søknadJson = "bob",
                                                  fnr = "ded",
                                                  dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD))

        dokumentasjonsbehovRepository.save(Dokumentasjonsbehov(søknad.id, "data"))
        val dokumentasjonsbehov = dokumentasjonsbehovRepository.findByIdOrNull(søknad.id)
        assertThat(dokumentasjonsbehov).isNotNull
        assertThat(dokumentasjonsbehov?.data).isEqualTo("data")
    }

}

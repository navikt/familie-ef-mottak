package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

internal class DokumentasjonsbehovRepositoryTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var søknadRepository: SøknadRepository

    @Autowired
    lateinit var dokumentasjonsbehovRepository: DokumentasjonsbehovRepository

    @Test
    internal fun `lagre og hent dokumentasjonsbehov`() {
        val søknad =
            søknadRepository.insert(
                Søknad(
                    søknadJson = EncryptedString("bob"),
                    fnr = "ded",
                    dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                ),
            )

        dokumentasjonsbehovRepository.insert(Dokumentasjonsbehov(søknad.id, "data"))
        val dokumentasjonsbehov = dokumentasjonsbehovRepository.findByIdOrNull(søknad.id)
        assertThat(dokumentasjonsbehov).isNotNull
        assertThat(dokumentasjonsbehov?.data).isEqualTo("data")
    }
}

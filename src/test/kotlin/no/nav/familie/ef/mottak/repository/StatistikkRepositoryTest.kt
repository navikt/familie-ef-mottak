package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

internal class StatistikkRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var søknadRepository: SøknadRepository
    @Autowired lateinit var statistikkRepository: StatistikkRepository

    @Test
    internal fun `søknader - finnes ikke noen`() {
        assertThat(statistikkRepository.antallSøknaderPerDokumentType()).isEmpty()
    }

    @Test
    internal fun `søknader - to søknader av overgangsstønad`() {
        søknadRepository.insert(Søknad(søknadJson = EncryptedString("{}"), dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD, fnr = ""))
        søknadRepository.insert(Søknad(søknadJson = EncryptedString("{}"), dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD, fnr = ""))
        val statistikk = statistikkRepository.antallSøknaderPerDokumentType()
        assertThat(statistikk).hasSize(1)
        assertThat(statistikk[0].antall).isEqualTo(2)
    }
}
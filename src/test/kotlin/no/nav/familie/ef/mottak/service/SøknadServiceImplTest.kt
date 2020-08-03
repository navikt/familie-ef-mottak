package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.Testdata.skjemaForArbeidssøker
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class SøknadServiceImplTest : IntegrasjonSpringRunnerTest() {

    @Autowired(required = true) lateinit var søknadService: SøknadService

    @Test
    internal fun `lagre skjema for arbeidssøker`() {
        val kvittering = søknadService.motta(skjemaForArbeidssøker)
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, emptyList()), emptyMap())
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }
}

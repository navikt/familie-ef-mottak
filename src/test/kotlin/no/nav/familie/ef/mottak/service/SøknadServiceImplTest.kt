package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.service.Testdata.skjemaForArbeidssøker
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class SøknadServiceImplTest : IntegrasjonSpringRunnerTest() {

    @Autowired(required = true) lateinit var søknadService: SøknadService
    @Autowired(required = true) lateinit var soknadRepository: SoknadRepository

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

    @Test
    internal fun `lagre skjema for søknad overgangsstønad med vedlegg`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg),
                                                            vedlegg.map { it.id to it.navn.toByteArray() }.toMap())
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad barnetilsyn`() {
        val kvittering = søknadService.mottaBarnetilsyn(SøknadMedVedlegg(søknadBarnetilsyn, emptyList()), emptyMap())
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }
}

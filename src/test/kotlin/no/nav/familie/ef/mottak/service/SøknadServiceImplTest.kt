package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.Testdata.skjemaForArbeidssøker
import no.nav.familie.ef.mottak.service.Testdata.søknad
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class SøknadServiceImplTest : IntegrasjonSpringRunnerTest() {

    @Autowired(required = true) lateinit var søknadService: SøknadService
    @Autowired(required = true) lateinit var pdfService: PdfService

    @Test
    internal fun `lagre skjema for arbeidssøker`() {
        val kvittering = søknadService.motta(skjemaForArbeidssøker)
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad`() {
        val kvittering = søknadService.motta(SøknadMedVedlegg(søknad, vedlegg), vedlegg.map { it.id to "ve".toByteArray() }.toMap())
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
        val lagPdf = pdfService.lagPdf(kvittering.id)
        println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(lagPdf))
    }
}

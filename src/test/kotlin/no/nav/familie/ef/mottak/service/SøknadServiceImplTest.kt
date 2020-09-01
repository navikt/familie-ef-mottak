package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.api.ApiFeil
import no.nav.familie.ef.mottak.service.Testdata.skjemaForArbeidssøker
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.søknadSkolepenger
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

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

    @Test
    internal fun `lagre skjema for søknad overgangsstønad med vedlegg`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg),
                                                            vedlegg.map { it.id to it.navn.toByteArray() }.toMap())
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }


    @Test
    internal fun `lagre skjema for skolepenger`() {
        val kvittering = søknadService.mottaSkolepenger(SøknadMedVedlegg(søknadSkolepenger, emptyList()), emptyMap())
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad skolepenger med vedlegg`() {
        val kvittering = søknadService.mottaSkolepenger(SøknadMedVedlegg(søknadSkolepenger, vedlegg),
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

    @Test
    internal fun `hent dokumentasjonsbehov til søknad`() {
        val dokumentasjonsbehov = listOf(Dokumentasjonsbehov("label", "id", false, emptyList()))
        val søknad = søknadOvergangsstønad
        val kvittering =
                søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknad, emptyList(), dokumentasjonsbehov),
                                                   emptyMap())
        val dokumentasjonsbehovDto = søknadService.hentDokumentasjonsbehovForSøknad(UUID.fromString(kvittering.id))

        assertThat(dokumentasjonsbehovDto.personIdent).isEqualTo(søknad.personalia.verdi.fødselsnummer.verdi.verdi)
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov).hasSize(1)
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov[0].harSendtInn).isFalse
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov[0].opplastedeVedlegg).isEmpty()
        assertThat(dokumentasjonsbehovDto.søknadType).isEqualTo(SøknadType.OVERGANGSSTØNAD)
    }

    @Test
    internal fun `hent dokumentasjonsbehov til søknad feiler når søknadId ikke finnes`() {
        assertThat(catchThrowable { søknadService.hentDokumentasjonsbehovForSøknad(UUID.randomUUID()) })
                .hasMessageContaining("Fant ikke dokumentasjonsbehov for søknad ")
                .isInstanceOf(ApiFeil::class.java)
    }

}

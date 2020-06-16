package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SøknadTreeWalkerTest {

    @Test
    fun `finnDokumenter finner alle dokumenter i en struktur`() {
        val søknad = Testdata.søknad

        val list = SøknadTreeWalker.finnDokumenter(søknad).map { it.toString() }

        assertThat(list).contains("giftIUtlandetDokumentasjon",
                                  "separertEllerSkiltIUtlandetDokumentasjon",
                                  "Skilsmisse- eller separasjonsbevilling",
                                  "Avtale om delt bosted for barna",
                                  "Erklæring om samlivsbrudd",
                                  "Bekreftelse på ventet fødselsdato",
                                  "Legeerklæring",
                                  "Legeattest for egen sykdom eller sykt barn",
                                  "Avslag på søknad om barnehageplass, skolefritidsordning e.l.",
                                  "Dokumentasjon av særlig tilsynsbehov",
                                  "Dokumentasjon av jobbtilbud",
                                  "Dokumentasjon av studieopptak",
                                  "Dokumentasjon av arbeidsforhold")
    }

    @Test
    fun `finnDokumenter returnerer en tom liste hvis det ikke finnes noen dokumenter i en struktur`() {
        val skjemaForArbeidssøker = Testdata.skjemaForArbeidssøker

        val list = SøknadTreeWalker.finnDokumenter(skjemaForArbeidssøker).map { it.toString() }

        assertThat(list).isEmpty()
    }

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden`() {
        val søknad = Testdata.søknad

        val mapSøknadsfelter = SøknadTreeWalker.mapSøknadsfelter(søknad)

        assertThat(mapSøknadsfelter).isNotEmpty
        assertThat(mapSøknadsfelter["label"]).isEqualTo("Søknad enslig forsørger")
        assertThat(mapSøknadsfelter["verdiliste"] as List<Any?>).hasSize(10)
    }

    @Test
    fun `mapSkjemafelter returnerer en map-struktur med feltene fra skjema`() {
        val skjemaForArbeidssøker = Testdata.skjemaForArbeidssøker

        val mapSøknadsfelter = SøknadTreeWalker.mapSkjemafelter(skjemaForArbeidssøker)

        assertThat(mapSøknadsfelter).isNotEmpty
        assertThat(mapSøknadsfelter["label"]).isEqualTo("Skjema for arbeidssøker - 15-08.01")
        assertThat(mapSøknadsfelter["verdiliste"] as List<Any?>).hasSize(3)
    }


}

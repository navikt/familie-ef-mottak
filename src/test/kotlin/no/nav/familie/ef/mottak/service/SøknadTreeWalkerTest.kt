package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SøknadTreeWalkerTest {

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden`() {
        val søknad = Testdata.søknad

        val mapSøknadsfelter = SøknadTreeWalker.mapSøknadsfelter(søknad, emptyList())

        assertThat(mapSøknadsfelter).isNotEmpty
        assertThat(mapSøknadsfelter["label"]).isEqualTo("Søknad enslig forsørger")

        val verdiliste = mapSøknadsfelter["verdiliste"] as List<Map<String, Any?>>
        assertThat(verdiliste).hasSize(11)

        val sivilstand = verdiliste.first { it["label"] == "Detaljer om sivilstand" }["verdiliste"]
        val verdi = (sivilstand as List<Map<String, Any>>).first { it["label"] == "giftIUtlandetDokumentasjon" }["verdi"]
        assertThat(verdi).isEqualTo("Har allerede sendt inn dokumentasjon: Nei")
                .withFailMessage("Dokumentasjon skal kun mappe verdiet på om man har sendt inn data tidligere")
    }

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden sammen med vedlegg`() {
        val søknad = Testdata.søknad

        val vedlegg = listOf(Vedlegg("id", "navn.pdf", "tittel", byteArrayOf(12)))
        val mapSøknadsfelter = SøknadTreeWalker.mapSøknadsfelter(søknad, vedlegg)

        assertThat(mapSøknadsfelter).isNotEmpty
        assertThat(mapSøknadsfelter["label"]).isEqualTo("Søknad enslig forsørger")
        assertThat(mapSøknadsfelter["verdiliste"] as List<Any?>).hasSize(11)
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

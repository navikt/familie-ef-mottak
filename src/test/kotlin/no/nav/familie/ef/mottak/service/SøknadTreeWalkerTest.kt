package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.TestUtils.readFile
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class SøknadTreeWalkerTest {

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden`() {
        val søknad = Testdata.søknad

        val mapSøknadsfelter = SøknadTreeWalker.mapSøknadsfelter(søknad, emptyList())

        assertThat(mapSøknadsfelter).isNotEmpty
        assertThat(mapSøknadsfelter["label"]).isEqualTo("Søknad enslig forsørger")

        val verdiliste = mapSøknadsfelter["verdiliste"] as List<Map<String, Any?>>
        assertThat(verdiliste).hasSize(11)
    }

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden sammen med vedlegg`() {
        val søknad = Testdata.søknad

        val vedlegg = listOf(Vedlegg("id", "navn.pdf", "Dokumentasjon på at du er syk", byteArrayOf(12)))
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

    @Test
    fun `mapSøknadsfelter printer pdf for å se endringer i pdf-genereringen i PR`() {
        val søknad = Testdata.søknad

        val vedlegg = listOf(Vedlegg("id", "navn.pdf", "Dokumentasjon på at du er syk", byteArrayOf(12)),
                             Vedlegg("id", "navn.pdf", "Dokumentasjon på at du er syk", byteArrayOf(12)),
                             Vedlegg("id", "navn.pdf", "Dokumentasjon på at kan arbeide", byteArrayOf(12)))
        val mapSøknadsfelter = SøknadTreeWalker.mapSøknadsfelter(søknad, vedlegg)
        val pdf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapSøknadsfelter)
        //Files.write(Path.of("src/test/resources/json/pdf_generated.json"), pdf.toByteArray()) //kommentere ut for å skrive over fila
        assertThat(pdf).isEqualTo(readFile("pdf_generated.json"))
    }

}

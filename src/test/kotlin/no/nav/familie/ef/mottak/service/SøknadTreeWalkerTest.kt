package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.IOTestUtil.readFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.FeltMap
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SøknadTreeWalkerTest {
    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden`() {
        val søknad = Testdata.søknadOvergangsstønad

        val mapSøknadsfelter = SøknadTreeWalker.mapOvergangsstønad(søknad, emptyList())

        assertThat(mapSøknadsfelter.verdiliste).isNotEmpty
        assertThat(mapSøknadsfelter.label).isEqualTo("Søknad om overgangsstønad (NAV 15-00.01)")
        assertThat(mapSøknadsfelter.verdiliste).hasSize(12)
    }

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra søknaden sammen med vedlegg`() {
        val søknad = Testdata.søknadOvergangsstønad

        val vedlegg = listOf("Dokumentasjon på at du er syk")
        val mapSøknadsfelter = SøknadTreeWalker.mapOvergangsstønad(søknad, vedlegg)

        assertThat(mapSøknadsfelter.verdiliste).isNotEmpty
        assertThat(mapSøknadsfelter.label).isEqualTo("Søknad om overgangsstønad (NAV 15-00.01)")
        assertThat(mapSøknadsfelter.verdiliste).hasSize(12)
    }

    @Test
    fun `mapSøknadsfelter returnerer en map-struktur med feltene fra skolepengesøknaden sammen med vedlegg`() {
        val søknad = Testdata.søknadSkolepenger

        val vedlegg = listOf("Dokumentasjon på at du er syk")
        val mapSøknadsfelter = SøknadTreeWalker.mapSkolepenger(søknad, vedlegg)

        assertThat(mapSøknadsfelter.verdiliste).isNotEmpty
        assertThat(mapSøknadsfelter.label).isEqualTo("Søknad om stønad til skolepenger (NAV 15-00.04)")
        assertThat(mapSøknadsfelter.verdiliste).hasSize(9)
    }

    @Test
    fun `mapSkjemafelter returnerer en map-struktur med feltene fra skjema`() {
        val skjemaForArbeidssøker = Testdata.skjemaForArbeidssøker

        val mapSøknadsfelter = SøknadTreeWalker.mapSkjemafelter(skjemaForArbeidssøker)

        assertThat(mapSøknadsfelter.verdiliste).isNotEmpty
        assertThat(mapSøknadsfelter.label).isEqualTo("Skjema for arbeidssøker - 15-08.01")
        assertThat(mapSøknadsfelter.verdiliste).hasSize(3)
    }

    @Test
    fun `mapSøknadsfelter printer pdf for å se endringer i pdf-genereringen i PR - overgangsstønad`() {
        val søknad = Testdata.søknadOvergangsstønad

        val vedlegg =
            listOf(
                "Dokumentasjon på at du er syk",
                "Dokumentasjon på at du er syk",
                "Dokumentasjon på at kan arbeide",
            )
        val mapSøknadsfelter = SøknadTreeWalker.mapOvergangsstønad(søknad, vedlegg)
        generatePdfAndAssert(mapSøknadsfelter, "pdf_generated_overgangsstønad.json")
    }

    @Test
    fun `mapSøknadsfelter printer pdf for å se endringer i pdf-genereringen i PR - skolepenger`() {
        val søknad = Testdata.søknadSkolepenger

        val vedlegg = listOf("Utgifter til utdanning")
        val mapSøknadsfelter = SøknadTreeWalker.mapSkolepenger(søknad, vedlegg)
        generatePdfAndAssert(mapSøknadsfelter, "pdf_generated_skolepenger.json")
    }

    @Test
    fun `mapSøknadsfelter printer pdf for å se endringer i pdf-genereringen i PR - barnetilsyn`() {
        val søknad = Testdata.søknadBarnetilsyn

        val vedlegg =
            listOf(
                "Dokumentasjon på at du er syk",
                "Dokumentasjon på at du er syk",
                "Dokumentasjon på at kan arbeide",
            )
        val mapSøknadsfelter = SøknadTreeWalker.mapBarnetilsyn(søknad, vedlegg)
        generatePdfAndAssert(mapSøknadsfelter, "pdf_generated_barnetilsyn.json")
    }

    @Test
    fun `map ettersending med vedlegg`() {
        val mapEttersending =
            SøknadTreeWalker.mapEttersending(
                Ettersending(
                    stønadType = "OVERGANGSSTØNAD",
                    fnr = "23118612345",
                    ettersendingJson = EncryptedString(""),
                    opprettetTid = LocalDateTime.of(2021, 5, 1, 12, 0),
                ),
                listOf("Lærlingkontrakt", "Utgifter til pass av barn"),
            )

        generatePdfAndAssert(mapEttersending, "pdf_generated_ettersending.json")
    }

    private fun generatePdfAndAssert(
        mapSøknadsfelter: FeltMap,
        filename: String,
    ) {
        val pdf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapSøknadsfelter)
        // kommentere ut for å skrive over fila
        java.nio.file.Files.write(java.nio.file.Path.of("src/test/resources/json/$filename"), pdf.toByteArray())
        assertThat(pdf).isEqualToIgnoringWhitespace(readFile(filename))
    }
}

package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Adresse
import no.nav.familie.kontrakter.ef.søknad.Datoperiode
import no.nav.familie.kontrakter.ef.søknad.Fødselsnummer
import no.nav.familie.kontrakter.ef.søknad.MånedÅrPeriode
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

internal class FeltformatererTest {

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer Month korrekt`() {
        val testverdi = Søknadsfelt("label", Month.DECEMBER)

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "desember"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer Boolean korrekt`() {
        val testverdi = Søknadsfelt("label", true)

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "Ja"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer liste med Boolean korrekt`() {
        val testverdi = Søknadsfelt("label", listOf(true, false))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "Ja\n\nNei"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer List korrekt`() {
        val testverdi = Søknadsfelt<List<*>>("label", listOf("Lille", "Grimme", "Arne", "Trenger", "Ris"))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "Lille\n\nGrimme\n\nArne\n\nTrenger\n\nRis"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer Fødselsnummer korrekt`() {
        val fnr = FnrGenerator.generer()
        val testverdi = Søknadsfelt("label", Fødselsnummer(fnr))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to fnr))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer Adresse korrekt`() {
        val testverdi = Søknadsfelt("label", Adresse("Husebyskogen 15", "1572", "Fet", "Norge"))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "Husebyskogen 15\n\n1572 Fet\n\nNorge"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer LocalDate korrekt`() {
        val testverdi = Søknadsfelt<LocalDate>("label", LocalDate.of(2015, 12, 5))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "05.12.2015"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer LocalDateTime korrekt`() {
        val testverdi = Søknadsfelt<LocalDateTime>("label", LocalDateTime.of(2015, 12, 5, 14, 52, 48))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "05.12.2015 14:52:48"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer MånedÅrPeriode korrekt`() {
        val testverdi = Søknadsfelt("label", MånedÅrPeriode(Month.FEBRUARY, 2015, Month.JULY, 2018))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "Fra februar 2015 til juli 2018"))
    }

    @Test
    fun `mapEndenodeTilUtskriftMap formaterer Datoperiode korrekt`() {
        val testverdi = Søknadsfelt("label", Datoperiode(LocalDate.of(2015, 2, 1), LocalDate.of(2018, 7, 14)))

        val resultat = Feltformaterer.mapEndenodeTilUtskriftMap(testverdi)

        assertThat(resultat).isEqualTo(mapOf("label" to "label", "verdi" to "Fra 01.02.2015 til 14.07.2018"))
    }
}

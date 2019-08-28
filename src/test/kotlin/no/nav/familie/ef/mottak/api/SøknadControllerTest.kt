package no.nav.familie.ef.mottak.api

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.api.dto.Søknad
import no.nav.familie.ef.mottak.integration.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SøknadControllerTest {

    private val søknadService: SøknadService = mockk()

    private val søknadController = SøknadController(søknadService)

    @Test
    fun `sendInn returnerer samme kvittering som returneres fra søknadService`() {
        val søknad = Søknad("tekst")
        every { søknadService.sendInn(søknad) } returns Kvittering("Mottatt søknad: ${søknad.text}")

        val kvittering = søknadController.sendInn(søknad)

        assertThat(kvittering.text).isEqualTo("Mottatt søknad: ${søknad.text}")
    }
}
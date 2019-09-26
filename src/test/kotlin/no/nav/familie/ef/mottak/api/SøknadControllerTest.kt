package no.nav.familie.ef.mottak.api

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.MottakServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SøknadControllerTest {

    private val søknadServiceImpl: MottakServiceImpl = mockk()

    private val søknadController = SøknadController(søknadServiceImpl)

    @Test
    fun `sendInn returnerer samme kvittering som returneres fra søknadService`() {
        val søknad = """{"tekst":"søknad""}"""
        every { søknadServiceImpl.motta(søknad) } returns Kvittering("Mottatt søknad: $søknad")

        val kvittering = søknadController.sendInn(søknad)

        assertThat(kvittering.text).isEqualTo("Mottatt søknad: $søknad")
    }
}

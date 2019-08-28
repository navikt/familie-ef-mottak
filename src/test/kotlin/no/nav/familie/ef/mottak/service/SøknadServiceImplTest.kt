package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.api.dto.Søknad
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.integration.dto.Kvittering
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SøknadServiceImplTest {

    private val søknadClient: SøknadClient = mockk()

    private val søknadService = SøknadServiceImpl(søknadClient)

    private val søknad = Søknad("Dette er en søknad")
    private val kvittering = Kvittering("Dette er en kvittering")

    @BeforeEach
    private fun init() {
        every { søknadClient.sendInn(søknad) } returns kvittering
    }


    @Test
    fun `sendInn skal kalle søknadClient for å sende inn søknad`() {
        søknadService.sendInn(søknad)

        verify { søknadClient.sendInn(søknad) }
    }

    @Test
    fun `sendInn skal returnere samme kvittering som kvittering fra søknadClient`() {
        val kvittering = søknadService.sendInn(søknad)

        assertThat(kvittering).isEqualTo(this.kvittering)
    }

}
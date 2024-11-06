package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class SøknadskvitteringServiceTest {
    private val søknadRepository = mockk<SøknadRepository>()
    private val vedleggRepository = mockk<VedleggRepository>()
    private val søknadkvitteringService = SøknadskvitteringService(søknadRepository, vedleggRepository)

    @Before
    fun setup() {
        every { søknadRepository.findByIdOrThrow("1") } returns
            søknad(
                søknadJsonString = EncryptedString(objectMapper.writeValueAsString(Testdata.søknadOvergangsstønad)),
            )
        every { vedleggRepository.finnTitlerForSøknadId("1") } returns listOf("")
    }

    @Test
    fun `skal hente søknad fra repository og mappe om til generelt format`() {
        val resultat = søknadkvitteringService.hentSøknadOgMapTilGenereltFormat("1")
        assertTrue(resultat.values.firstOrNull() == "Søknad om overgangsstønad (NAV 15-00.01)")
    }
}

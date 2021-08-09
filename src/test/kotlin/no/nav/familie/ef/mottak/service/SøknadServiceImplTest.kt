package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.ef.mottak.service.SøknadServiceImpl
import no.nav.familie.ef.mottak.service.Testdata
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov as DokumentasjonsbehovKontrakter
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class SøknadServiceImplTest {

    val søknadRepository = mockk<SøknadRepository>()
    val dokumentasjonsbehovRepository = mockk<DokumentasjonsbehovRepository>()
    val søknadService = SøknadServiceImpl(søknadRepository, mockk(), dokumentasjonsbehovRepository, mockk())

    @Test
    internal fun `hentDokumentasjonsbehovforPerson fungerer for overgangsstønad, barnetilsyn og skolepenger`() {
        val fnr = "12345678"
        val søknader = listOf(
                SøknadMapper.fromDto(Testdata.søknadOvergangsstønad, false),
                SøknadMapper.fromDto(Testdata.søknadBarnetilsyn, false),
                SøknadMapper.fromDto(Testdata.søknadSkolepenger, false),
                SøknadMapper.fromDto(Testdata.skjemaForArbeidssøker),
        )
        val forventetDokumentasjonsbehov = listOf(DokumentasjonsbehovKontrakter(
                "test",
                UUID.randomUUID()
                        .toString(),
                false))

        every { søknadRepository.findAllByFnr(fnr) } returns søknader

        every { dokumentasjonsbehovRepository.findByIdOrNull(any()) } returns Dokumentasjonsbehov("123",
                                                                                                  objectMapper.writeValueAsString(
                                                                                                          forventetDokumentasjonsbehov))

        every { søknadRepository.findByIdOrNull(any()) } returns SøknadMapper.fromDto(Testdata.søknadOvergangsstønad, false)


        assertThat(søknadService.hentDokumentasjonsbehovForPerson(fnr)).hasSize(3)
    }
}
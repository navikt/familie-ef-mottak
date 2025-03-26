package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.service.Testdata.skjemaForArbeidssøker
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.søknadSkolepenger
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.ef.mottak.util.DatoUtil
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.Innsendingsdetaljer
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.Fødselsnummer
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertNotEquals

internal class SøknadServiceIntegrasjonTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var søknadService: SøknadService

    @Autowired
    lateinit var vedleggRepository: VedleggRepository

    @Autowired
    lateinit var dokumentasjonsbehovRepository: DokumentasjonsbehovRepository

    @Test
    internal fun `lagre skjema for arbeidssøker`() {
        val kvittering = søknadService.mottaArbeidssøkerSkjema(skjemaForArbeidssøker)
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat((søknad)).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad`() {
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, emptyList()))
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad overgangsstønad med vedlegg`() {
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg))
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for skolepenger`() {
        val kvittering = søknadService.mottaSøknadSkolepenger(SøknadMedVedlegg(søknadSkolepenger, emptyList()))
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad skolepenger med vedlegg`() {
        val kvittering = søknadService.mottaSøknadSkolepenger(SøknadMedVedlegg(søknadSkolepenger, vedlegg))
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `skal kunne behandle sak med barn under 6 mnd i ny løsning`() {
        val barnFødtIDag =
            listOf(
                søknadOvergangsstønad.barn.verdi.first().copy(
                    fødselTermindato = Søknadsfelt("Termindato", LocalDate.now()),
                ),
            )
        val søknadMedBarnFødtIDag =
            søknadOvergangsstønad.copy(
                barn = Søknadsfelt("Barn", barnFødtIDag),
            )
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadMedBarnFødtIDag, vedlegg))

        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
        assertThat(søknad.behandleINySaksbehandling).isTrue
    }

    @Test
    internal fun `skal kunne behandle sak med et barn under 6 mnd og et over i ny løsning`() {
        val barnFødtIDag =
            søknadOvergangsstønad.barn.verdi.first().copy(
                fødselTermindato = Søknadsfelt("Termindato", LocalDate.now()),
            )

        val barnFødtforLengeSiden =
            søknadOvergangsstønad.barn.verdi.first().copy(
                fødselTermindato = Søknadsfelt("Termindato", LocalDate.now().minusYears(3)),
            )
        val søknadMedBarnFødtIDag =
            søknadOvergangsstønad.copy(
                barn = Søknadsfelt("Barn", listOf(barnFødtforLengeSiden, barnFødtIDag)),
            )
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadMedBarnFødtIDag, vedlegg))

        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
        assertThat(søknad.behandleINySaksbehandling).isTrue
    }

    @Test
    internal fun `skal kunne behandle sak med barn under 6 mnd i ny løsning gitt ident `() {
        val fødselsdato = LocalDate.now().minusMonths(5)
        val ident = FnrGenerator.generer(fødselsdato.year, fødselsdato.month.value, fødselsdato.dayOfMonth)

        val barn1mnd =
            listOf(
                søknadOvergangsstønad.barn.verdi.first().copy(
                    fødselsnummer = Søknadsfelt("Fødselsnummer", Fødselsnummer(ident)),
                ),
            )
        val søknadMedBarn1mnd =
            søknadOvergangsstønad.copy(
                barn = Søknadsfelt("Barn", barn1mnd),
            )
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadMedBarn1mnd, vedlegg))
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
        assertThat(søknad.behandleINySaksbehandling).isTrue
    }

    @Test
    internal fun `lagre skjema for søknad barnetilsyn`() {
        val kvittering = søknadService.mottaSøknadBarnetilsyn(SøknadMedVedlegg(søknadBarnetilsyn, emptyList()))
        val søknad = søknadService.hentSøknad(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `hent dokumentasjonsbehov til søknad`() {
        val dokumentasjonsbehov = listOf(Dokumentasjonsbehov("label", "id", false, emptyList()))
        val søknad = søknadOvergangsstønad
        val kvittering =
            søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknad, emptyList(), dokumentasjonsbehov))
        val dokumentasjonsbehovDto = søknadService.hentDokumentasjonsbehovForSøknad(søknadService.hentSøknad(kvittering.id))

        assertThat(dokumentasjonsbehovDto.personIdent).isEqualTo(søknad.personalia.verdi.fødselsnummer.verdi.verdi)
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov).hasSize(1)
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov[0].harSendtInn).isFalse
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov[0].opplastedeVedlegg).isEmpty()
        assertThat(dokumentasjonsbehovDto.søknadType).isEqualTo(SøknadType.OVERGANGSSTØNAD)
    }

    @Test
    fun `reduserSøknad sletter søknadPdf, dokumentasjonsbehov og vedlegg for gitt søknadId`() {
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg))
        val søknadFørReduksjon = søknadService.hentSøknad(kvittering.id)
        søknadService.oppdaterSøknad(søknadFørReduksjon.copy(søknadPdf = EncryptedFile(ByteArray(20)), journalpostId = "321"))

        søknadService.reduserSøknad(søknadFørReduksjon.id)

        val søknad = søknadService.hentSøknad(søknadFørReduksjon.id)
        assertThat(dokumentasjonsbehovRepository.findByIdOrNull(søknad.id)).isNull()
        assertThat(vedleggRepository.findBySøknadId(søknad.id)).isEmpty()
    }

    @Test
    fun `slettSøknad sletter søknad for gitt søknadId`() {
        val kvittering = søknadService.mottaSøknadOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg))
        val søknad = søknadService.hentSøknad(kvittering.id)
        søknadService.oppdaterSøknad(søknad.copy(journalpostId = "321"))
        søknadService.reduserSøknad(søknad.id)

        søknadService.slettSøknad(søknad.id)

        assertThrows<IllegalStateException> { (søknadService.hentSøknad(søknad.id)) }
    }

    @Test
    fun `skal returnere tom liste med innnsendte søknader for førstegangsøker`() {
        val personIdent = "03125462714"
        val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

        assertThat(søknader.isEmpty())
    }

    @Test
    fun `skal returnere liste med innnsendt søknad som er innsendt innen 30 dager`() {
        val personIdent = "03125462714"
        val søknadMedVedlegg = genererOvergangstønadSøknadMedVedlegg(LocalDateTime.now())

        søknadService.mottaSøknadOvergangsstønad(søknadMedVedlegg)

        val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

        assertThat(søknader).hasSize(1)
        assertThat(søknader.first { it.stønadType == StønadType.OVERGANGSSTØNAD })
    }

    @Test
    fun `skal returnere tom liste der innsendt søknad ble innsendt for mer enn 30 dager siden`() {
        mockkObject(DatoUtil)
        every { DatoUtil.dagensDatoMedTid() } returns LocalDateTime.now().minusDays(31)

        val personIdent = "03125462714"
        val søknadMedVedlegg = genererOvergangstønadSøknadMedVedlegg(DatoUtil.dagensDatoMedTid())

        søknadService.mottaSøknadOvergangsstønad(søknadMedVedlegg)

        val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

        assertThat(søknader).isEmpty()
        unmockkObject(DatoUtil)
    }

    @Test
    fun `skal returnere nyeste innsendte søknad av samme stønadtype`() {
        val personIdent = "03125462714"

        val tidligereSøknad = genererOvergangstønadSøknadMedVedlegg(LocalDateTime.now().minusDays(7))
        val nyesteSøknad = genererOvergangstønadSøknadMedVedlegg(LocalDateTime.now())

        søknadService.mottaSøknadOvergangsstønad(tidligereSøknad)
        søknadService.mottaSøknadOvergangsstønad(nyesteSøknad)

        val søknader = søknadService.hentSistInnsendtSøknadPerStønad(personIdent)

        val nyesteSøknadDato = søknader.first().søknadsdato
        val tidligereSøknadDato =
            tidligereSøknad.søknad.innsendingsdetaljer.verdi.datoMottatt.verdi
                .toLocalDate()

        assertThat(søknader).hasSize(1)
        assertThat(søknader.first { it.stønadType == StønadType.OVERGANGSSTØNAD })
        assertNotEquals(nyesteSøknadDato, tidligereSøknadDato)
    }

    private fun genererOvergangstønadSøknadMedVedlegg(
        datoMottatt: LocalDateTime,
    ): SøknadMedVedlegg<SøknadOvergangsstønad> {
        val søknad =
            søknadOvergangsstønad.copy(
                innsendingsdetaljer =
                    Søknadsfelt(
                        label = "detaljer",
                        verdi =
                            Innsendingsdetaljer(
                                datoMottatt =
                                    Søknadsfelt(
                                        label = "mottat",
                                        verdi = datoMottatt,
                                    ),
                                datoPåbegyntSøknad = null,
                            ),
                    ),
            )

        return SøknadMedVedlegg(
            søknad = søknad,
            vedlegg = emptyList(),
        )
    }
}

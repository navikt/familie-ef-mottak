package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.service.Testdata.skjemaForArbeidssøker
import no.nav.familie.ef.mottak.service.Testdata.søknadBarnetilsyn
import no.nav.familie.ef.mottak.service.Testdata.søknadOvergangsstønad
import no.nav.familie.ef.mottak.service.Testdata.søknadSkolepenger
import no.nav.familie.ef.mottak.service.Testdata.vedlegg
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.Innsendingsdetaljer
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.Fødselsnummer
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime

internal class SøknadServiceIntegrasjonTest : IntegrasjonSpringRunnerTest() {
    @Autowired
    lateinit var søknadService: SøknadService

    @Autowired
    lateinit var vedleggRepository: VedleggRepository

    @Autowired
    lateinit var dokumentasjonsbehovRepository: DokumentasjonsbehovRepository

    @Test
    internal fun `lagre skjema for arbeidssøker`() {
        val kvittering = søknadService.mottaSkjemaForArbeidssøker(skjemaForArbeidssøker)
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, emptyList()))
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad overgangsstønad med vedlegg`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg))
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for skolepenger`() {
        val kvittering = søknadService.mottaSkolepenger(SøknadMedVedlegg(søknadSkolepenger, emptyList()))
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `lagre skjema for søknad skolepenger med vedlegg`() {
        val kvittering = søknadService.mottaSkolepenger(SøknadMedVedlegg(søknadSkolepenger, vedlegg))
        val søknad = søknadService.get(kvittering.id)
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
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadMedBarnFødtIDag, vedlegg))

        val søknad = søknadService.get(kvittering.id)
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
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadMedBarnFødtIDag, vedlegg))

        val søknad = søknadService.get(kvittering.id)
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
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadMedBarn1mnd, vedlegg))
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
        assertThat(søknad.behandleINySaksbehandling).isTrue
    }

    @Test
    internal fun `lagre skjema for søknad barnetilsyn`() {
        val kvittering = søknadService.mottaBarnetilsyn(SøknadMedVedlegg(søknadBarnetilsyn, emptyList()))
        val søknad = søknadService.get(kvittering.id)
        assertThat(søknad).isNotNull
    }

    @Test
    internal fun `hent dokumentasjonsbehov til søknad`() {
        val dokumentasjonsbehov = listOf(Dokumentasjonsbehov("label", "id", false, emptyList()))
        val søknad = søknadOvergangsstønad
        val kvittering =
            søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknad, emptyList(), dokumentasjonsbehov))
        val dokumentasjonsbehovDto = søknadService.hentDokumentasjonsbehovForSøknad(søknadService.get(kvittering.id))

        assertThat(dokumentasjonsbehovDto.personIdent).isEqualTo(søknad.personalia.verdi.fødselsnummer.verdi.verdi)
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov).hasSize(1)
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov[0].harSendtInn).isFalse
        assertThat(dokumentasjonsbehovDto.dokumentasjonsbehov[0].opplastedeVedlegg).isEmpty()
        assertThat(dokumentasjonsbehovDto.søknadType).isEqualTo(SøknadType.OVERGANGSSTØNAD)
    }

    @Test
    fun `reduserSøknad sletter søknadPdf, dokumentasjonsbehov og vedlegg for gitt søknadId`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg))
        val søknadFørReduksjon = søknadService.get(kvittering.id)
        søknadService.oppdaterSøknad(søknadFørReduksjon.copy(søknadPdf = EncryptedFile(ByteArray(20)), journalpostId = "321"))

        søknadService.reduserSøknad(søknadFørReduksjon.id)

        val søknad = søknadService.get(søknadFørReduksjon.id)
        assertThat(dokumentasjonsbehovRepository.findByIdOrNull(søknad.id)).isNull()
        assertThat(vedleggRepository.findBySøknadId(søknad.id)).isEmpty()
    }

    @Test
    fun `slettSøknad sletter søknad for gitt søknadId`() {
        val kvittering = søknadService.mottaOvergangsstønad(SøknadMedVedlegg(søknadOvergangsstønad, vedlegg))
        val søknad = søknadService.get(kvittering.id)
        søknadService.oppdaterSøknad(søknad.copy(journalpostId = "321"))
        søknadService.reduserSøknad(søknad.id)

        søknadService.slettSøknad(søknad.id)

        assertThrows<IllegalStateException> { (søknadService.get(søknad.id)) }
    }

    @Test
    fun `skal returnere tom liste med innnsendte søknader for førstegangsøker`() {
        val personIdent = "12345678910"
        val søknader = søknadService.hentSistInnsendteSøknadPerStønad(personIdent)

        assertThat(søknader.isEmpty())
    }

    @Test
    fun `skal returnere liste med innnsendt søknad som er innsendt innen 30 dager`() {
        val personIdent = "03125462714"

        val overgangStønadSøknad =
            søknadOvergangsstønad.copy(
                innsendingsdetaljer =
                    Søknadsfelt(
                        label = "detaljer",
                        verdi =
                            Innsendingsdetaljer(
                                datoMottatt =
                                    Søknadsfelt(
                                        label = "mottat",
                                        verdi = LocalDateTime.now(),
                                    ),
                                datoPåbegyntSøknad = null,
                            ),
                    ),
            )

        val søknadMedVedlegg =
            SøknadMedVedlegg(
                søknad = overgangStønadSøknad,
                vedlegg = emptyList(),
            )

        søknadService.mottaOvergangsstønad(søknadMedVedlegg)

        val søknader = søknadService.hentSistInnsendteSøknadPerStønad(personIdent)

        assertThat(søknader.size).isEqualTo(1)
        assertThat(søknader.first { it.stønadType == DOKUMENTTYPE_OVERGANGSSTØNAD })
    }

    @Test
    fun `skal returnere tom liste der søknad er eldre enn 30 dager`() {
        val personIdent = "03125462714"

        val overgangStønadSøknad =
            søknadOvergangsstønad.copy(
                innsendingsdetaljer =
                    Søknadsfelt(
                        label = "detaljer",
                        verdi =
                            Innsendingsdetaljer(
                                datoMottatt =
                                    Søknadsfelt(
                                        label = "mottat",
                                        verdi = LocalDateTime.now().minusDays(31),
                                    ),
                                datoPåbegyntSøknad = null,
                            ),
                    ),
            )

        val søknadMedVedlegg =
            SøknadMedVedlegg(
                søknad = overgangStønadSøknad,
                vedlegg = emptyList(),
            )

        søknadService.mottaOvergangsstønad(søknadMedVedlegg)

        val søknader = søknadService.hentSistInnsendteSøknadPerStønad(personIdent)

        assertThat(søknader.isEmpty())
        assertThat(søknader.first { it.stønadType == DOKUMENTTYPE_OVERGANGSSTØNAD })
    }
}

package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.ef.mottak.util.okEllerKastException
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.felles.søknad.SistInnsendtSøknadDto
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["api/soknadskvittering", "/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@ProtectedWithClaims(
    issuer = EksternBrukerUtils.ISSUER_TOKENX,
    claimMap = ["acr=Level4"],
)
class SøknadskvitteringController(
    val søknadKvitteringService: SøknadskvitteringService,
) {
    @PostMapping("overgangsstonad")
    fun mottaSøknadOvergangsstønad(
        @RequestBody søknad: SøknadMedVedlegg<SøknadOvergangsstønad>,
    ): Kvittering = okEllerKastException { søknadKvitteringService.mottaSøknadOvergangsstønad(søknad) }

    @PostMapping("barnetilsyn")
    fun mottaSøknadBarnetilsyn(
        @RequestBody søknad: SøknadMedVedlegg<SøknadBarnetilsyn>,
    ): Kvittering = okEllerKastException { søknadKvitteringService.mottaSøknadBarnetilsyn(søknad) }

    @PostMapping("skolepenger")
    fun mottaSøknadSkolepenger(
        @RequestBody søknad: SøknadMedVedlegg<SøknadSkolepenger>,
    ): Kvittering = okEllerKastException { søknadKvitteringService.mottaSøknadSkolepenger(søknad) }

    @PostMapping("arbeidssoker")
    fun mottaArbeidssøkerSkjema(
        @RequestBody skjemaForArbeidssøker: SkjemaForArbeidssøker,
    ): Kvittering = okEllerKastException { søknadKvitteringService.mottaArbeidssøkerSkjema(skjemaForArbeidssøker) }

    @GetMapping("barnetilsyn/forrige")
    fun hentBarnetilsynssøknadForPerson(): SøknadBarnetilsyn? {
        val personIdent = EksternBrukerUtils.hentFnrFraToken()
        return søknadKvitteringService.hentBarnetilsynSøknadsverdierTilGjenbruk(personIdent)
    }

    @GetMapping("sist-innsendt-per-stonad")
    fun hentSistInnsendtSøknadPerStønad(): List<SistInnsendtSøknadDto> {
        val personIdent = EksternBrukerUtils.hentFnrFraToken()
        return søknadKvitteringService.hentSistInnsendtSøknadPerStønad(personIdent)
    }
}

package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.service.SøknadskvitteringService
import no.nav.familie.ef.mottak.util.okEllerKastException
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@ProtectedWithClaims(
    issuer = EksternBrukerUtils.ISSUER_TOKENX,
    claimMap = ["acr=Level4"],
)
class SøknadController(
    val søknadService: SøknadService,
    val søknadskvitteringService: SøknadskvitteringService,
) {
    @PostMapping("overgangsstonad")
    fun overgangsstønad(
        @RequestBody søknad: SøknadMedVedlegg<SøknadOvergangsstønad>,
    ): Kvittering = okEllerKastException { søknadService.mottaOvergangsstønad(søknad) }

    @PostMapping("barnetilsyn")
    fun barnetilsyn(
        @RequestBody søknad: SøknadMedVedlegg<SøknadBarnetilsyn>,
    ): Kvittering = okEllerKastException { søknadService.mottaBarnetilsyn(søknad) }

    @PostMapping("skolepenger")
    fun skolepenger(
        @RequestBody søknad: SøknadMedVedlegg<SøknadSkolepenger>,
    ): Kvittering = okEllerKastException { søknadService.mottaSkolepenger(søknad) }

    @GetMapping("barnetilsyn/forrige")
    fun hentBarnetilsynssøknadForPerson(): SøknadBarnetilsyn? {
        val personIdent = EksternBrukerUtils.hentFnrFraToken()
        return søknadskvitteringService.hentBarnetilsynSøknadsverdierTilGjenbruk(personIdent)
    }

    @GetMapping("akt'ive")
    fun hentAktiveSøknader(): List<SistInnsendteSøknad> {
        val personIdent = EksternBrukerUtils.hentFnrFraToken()
        return søknadService.hentSistInnsendteSøknadPerStønad(personIdent)
    }
}

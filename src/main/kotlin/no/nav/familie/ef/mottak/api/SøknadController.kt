package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.util.getRootCause
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.RequiredIssuers
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(consumes = [MULTIPART_FORM_DATA_VALUE], path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@RequiredIssuers(
        ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER, claimMap = ["acr=Level4"]),
        ProtectedWithClaims(issuer = EksternBrukerUtils.ISSUER_TOKENX, claimMap = ["acr=Level4"])
)
class SøknadController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping(path = ["", "overgangsstonad"])
    fun overgangsstønad(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadOvergangsstønad>,
                        @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?)
            : ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return okEllerKastException { søknadService.mottaOvergangsstønad(søknad, vedleggData) }
    }

    @PostMapping(path = ["barnetilsyn"])
    fun barnetilsyn(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadBarnetilsyn>,
                    @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?)
            : ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return okEllerKastException { søknadService.mottaBarnetilsyn(søknad, vedleggData) }
    }

    @PostMapping(path = ["skolepenger"])
    fun skolepenger(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadSkolepenger>,
                    @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?)
            : ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)
        return okEllerKastException { søknadService.mottaSkolepenger(søknad, vedleggData) }
    }

    private fun okEllerKastException(producer: () -> Kvittering): ResponseEntity<Kvittering> {
        try {
            return ResponseEntity.ok(producer.invoke())
        } catch (e: Exception) {
            if (e.getRootCause()?.message == no.nav.familie.ef.mottak.repository.domain.Vedlegg.UPDATE_FEILMELDING) {
                throw ApiFeil("Det går ikke å sende inn samme vedlegg to ganger", HttpStatus.BAD_REQUEST)
            } else {
                throw e
            }
        }
    }

    private fun vedleggData(vedleggListe: List<MultipartFile>?): Map<String, ByteArray> =
            vedleggListe?.map { it.originalFilename to it.bytes }?.toMap() ?: emptyMap()

    private fun validerVedlegg(vedlegg: List<Vedlegg>,
                               vedleggData: Map<String, ByteArray>) {
        val vedleggMetadata = vedlegg.map { it.id to it }.toMap()
        if (vedleggMetadata.keys.size != vedleggData.keys.size || !vedleggMetadata.keys.containsAll(vedleggData.keys)) {
            logger.error("Søknad savner: [{}], vedleggListe:[{}]",
                         vedleggMetadata.keys.toMutableSet().removeAll(vedleggData.keys),
                         vedleggData.keys.toMutableSet().removeAll(vedleggMetadata.keys))
            throw ApiFeil("Savner vedlegg, se logg for mer informasjon", HttpStatus.BAD_REQUEST)
        }
    }

}

package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.security.token.support.core.api.Protected
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
@Protected
class SøknadController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping(path = ["", "overgangsstonad"])
    fun overgangsstønad(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadOvergangsstønad>,
                        @RequestPart("vedlegg") vedleggListe: List<MultipartFile>
    ): ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return ResponseEntity.ok(søknadService.mottaOvergangsstønad(søknad, vedleggData))
    }

    @PostMapping(path = ["barnetilsyn"])
    fun barnetilsyn(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadBarnetilsyn>,
                    @RequestPart("vedlegg") vedleggListe: List<MultipartFile>
    ): ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return ResponseEntity.ok(søknadService.mottaBarnetilsyn(søknad, vedleggData))
    }

    @PostMapping(path = ["skolepenger"])
    fun skolepenger(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadSkolepenger>,
                    @RequestPart("vedlegg") vedleggListe: List<MultipartFile>
    ): ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return ResponseEntity.ok(søknadService.mottaSkolepenger(søknad, vedleggData))
    }

    private fun vedleggData(vedleggListe: List<MultipartFile>) =
            vedleggListe.map { it.originalFilename to it.bytes }.toMap()

    private fun validerVedlegg(vedlegg: List<Vedlegg>,
                               vedleggData: Map<String?, ByteArray>) {
        val vedleggMetadata = vedlegg.map { it.id to it }.toMap()
        if (vedleggMetadata.keys.size != vedleggData.keys.size || !vedleggMetadata.keys.containsAll(vedleggData.keys)) {
            logger.error("Søknad savner: [{}], vedleggListe:[{}]",
                         vedleggMetadata.keys.toMutableSet().removeAll(vedleggData.keys),
                         vedleggData.keys.toMutableSet().removeAll(vedleggMetadata.keys))
            throw ApiFeil("Savner vedlegg, se logg for mer informasjon", HttpStatus.BAD_REQUEST)
        }
    }

}

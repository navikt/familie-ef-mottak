package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.FeilDto
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@Protected
class SøknadController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun sendInn(@RequestBody søknad: SøknadMedVedlegg): Kvittering {
        return søknadService.motta(søknad.copy(vedlegg = søknad.vedlegg.map { it.copy(bytes = null) }),
                                   søknad.vedlegg.map { it.id to it.bytes!! }.toMap())
    }

    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendInn(@RequestPart("søknad") søknad: SøknadMedVedlegg,
                @RequestPart("vedlegg") vedleggListe: List<MultipartFile>
    ): ResponseEntity<Any> {
        val vedleggMetadata = søknad.vedlegg.map { it.id to it }.toMap()
        val vedlegg = vedleggListe.map { it.originalFilename to it.bytes }.toMap()

        if (vedleggMetadata.keys.size != vedlegg.keys.size || !vedleggMetadata.keys.containsAll(vedlegg.keys)) {
            logger.error("Søknad savner: [{}], vedleggListe:[{}]",
                         vedleggMetadata.keys.toMutableSet().removeAll(vedlegg.keys),
                         vedlegg.keys.toMutableSet().removeAll(vedleggMetadata.keys))
            return ResponseEntity.badRequest().body(FeilDto("Savner vedlegg, se logg for mer informasjon"))
        }
        return ResponseEntity.ok(søknadService.motta(søknad, vedlegg))
    }

}

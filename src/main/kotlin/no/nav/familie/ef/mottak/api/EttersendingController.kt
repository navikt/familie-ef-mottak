package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.util.getRootCause
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import no.nav.familie.kontrakter.ef.søknad.Ettersending
import no.nav.familie.kontrakter.ef.søknad.EttersendingMedVedlegg
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(consumes = [MULTIPART_FORM_DATA_VALUE], path = ["/api"], produces = [APPLICATION_JSON_VALUE] )
@Protected
class EttersendingController(val ettersendingService: EttersendingService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

   @PostMapping(path = ["ettersending"])
    fun ettersending(@RequestPart("ettersending") ettersending: EttersendingMedVedlegg<Ettersending>,
                     @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?): ResponseEntity<Kvittering>
    {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(ettersending.vedlegg, vedleggData)

        return okEllerKastException { ettersendingService.mottaEttersending(ettersending, vedleggData) }
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
            logger.error("Ettersending savner: [{}], vedleggListe:[{}]",
                vedleggMetadata.keys.toMutableSet().removeAll(vedleggData.keys),
                vedleggData.keys.toMutableSet().removeAll(vedleggMetadata.keys))
            throw ApiFeil("Savner vedlegg, se logg for mer informasjon", HttpStatus.BAD_REQUEST)
        }
    }

}

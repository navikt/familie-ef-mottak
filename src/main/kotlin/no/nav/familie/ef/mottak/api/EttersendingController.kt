package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.EttersendingRequestData
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.util.okEllerKastException
import no.nav.familie.kontrakter.ef.ettersending.EttersendingMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.sikkerhet.EksternBrukerUtils
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(consumes = [MULTIPART_FORM_DATA_VALUE, APPLICATION_JSON_VALUE],
                path = ["/api/ettersending"],
                produces = [APPLICATION_JSON_VALUE])
@Protected
class EttersendingController(val ettersendingService: EttersendingService) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @PostMapping
    fun ettersending(@RequestPart("ettersending") ettersending: EttersendingMedVedlegg,
                     @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?): ResponseEntity<Kvittering> {
        val vedleggData = vedleggData(vedleggListe)
        validerVedlegg(ettersending.vedlegg, vedleggData)

        return okEllerKastException { ettersendingService.mottaEttersending(ettersending, vedleggData) }
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

    @PostMapping("hent-ettersending-for-person")
    fun hentEttersending(@RequestBody personIdent: PersonIdent): ResponseEntity<List<EttersendingRequestData>> {
        val fnrFraToken = EksternBrukerUtils.hentFnrFraToken()
        if (fnrFraToken != personIdent.ident) {
            logger.warn("Fødselsnummer fra token matcher ikke fnr på søknaden")
            secureLogger.info("TokenFnr={} matcher ikke søknadFnr={} søknadId={}",
                              fnrFraToken,
                              personIdent.ident)
            throw ApiFeil("Fnr fra token matcher ikke fnr på søknaden", HttpStatus.FORBIDDEN)
        }
        val ettersendingData = ettersendingService.hentEttersendingsdataForPerson(personIdent) // TODO må implementeres

        return ResponseEntity.ok(ettersendingData)
    }

}

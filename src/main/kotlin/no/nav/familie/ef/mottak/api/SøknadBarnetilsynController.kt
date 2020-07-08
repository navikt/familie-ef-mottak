package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.FeilDto
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.ef.søknad.Barn
import no.nav.familie.kontrakter.ef.søknad.Personalia
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(path = ["/api/barnetilsyn"], produces = [APPLICATION_JSON_VALUE])
@Protected
class SøknadBarnetilsynController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping(consumes = [MULTIPART_FORM_DATA_VALUE])
    fun sendInn(@RequestPart("søknad") søknad: SøknadBarnetilsyn,
                @RequestPart("vedlegg") vedleggListe: List<MultipartFile>
    ): ResponseEntity<Any> {
        val vedlegg = mapVedlegg(vedleggListe)
        if (erIkkeGyldig(søknad, vedlegg)) {
            return ResponseEntity.badRequest().body(FeilDto("Savner vedlegg, se logg for mer informasjon"))
        }
        return ResponseEntity.ok(søknadService.motta(søknad, vedlegg))
    }

    private fun mapVedleggMetadata(søknad: SøknadBarnetilsyn): Map<String, Vedlegg> {
        return søknad.vedlegg.map { it.id to it }.toMap()
    }

    private fun mapVedlegg(vedleggListe: List<MultipartFile>): Map<String?, ByteArray> {
        return vedleggListe.map { it.originalFilename to it.bytes }.toMap()
    }

    private fun erIkkeGyldig(søknad: SøknadBarnetilsyn,
                             vedlegg: Map<String?, ByteArray>): Boolean {
        val vedleggMetadata = mapVedleggMetadata(søknad)

        if (vedleggMetadata.keys.size != vedlegg.keys.size || !vedleggMetadata.keys.containsAll(vedlegg.keys)) {
            logger.error("Søknad savner: [{}], vedleggListe:[{}]",
                         vedleggMetadata.keys.toMutableSet().removeAll(vedlegg.keys),
                         vedlegg.keys.toMutableSet().removeAll(vedleggMetadata.keys))
            return true
        }
        return false
    }

}

// TODO Flyttes til kontrakter
data class SøknadBarnetilsyn(val søknad: BarnetilsynSøknadKontrakt, val vedlegg: List<Vedlegg>)
data class BarnetilsynSøknadKontrakt(val personalia: Søknadsfelt<Personalia>,
                                     val barn: Søknadsfelt<List<Barn>>)
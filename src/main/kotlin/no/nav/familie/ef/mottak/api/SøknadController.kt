package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Vedlegg.Companion
import no.nav.familie.ef.mottak.repository.domain.Vedlegg.Companion.UPDATE_FEILMELDING
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.util.getRootCause
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
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
@RequestMapping(path = ["/api/soknad"], produces = [APPLICATION_JSON_VALUE])
@Protected
class SøknadController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("overgangsstonad")
    fun overgangsstønad(@RequestBody søknad: SøknadMedVedlegg<SøknadOvergangsstønad>): Kvittering {
        return okEllerKastException { søknadService.mottaOvergangsstønad(søknad) }
    }

    @PostMapping("barnetilsyn")
    fun barnetilsyn(@RequestBody søknad: SøknadMedVedlegg<SøknadBarnetilsyn>): Kvittering {
        return okEllerKastException { søknadService.mottaBarnetilsyn(søknad) }
    }

    @PostMapping("skolepenger")
    fun skolepenger(@RequestBody søknad: SøknadMedVedlegg<SøknadSkolepenger>): Kvittering {
        return okEllerKastException { søknadService.mottaSkolepenger(søknad) }
    }

    @Deprecated("Bruk metode som henter vedlegg fra familie-dokument")
    @PostMapping(path = ["", "overgangsstonad"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun overgangsstønadOld(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadOvergangsstønad>,
                           @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?): Kvittering {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return okEllerKastException { søknadService.mottaOvergangsstønad(søknad, vedleggData) }
    }

    @Deprecated("Bruk metode som henter vedlegg fra familie-dokument")
    @PostMapping(path = ["barnetilsyn"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun barnetilsynOld(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadBarnetilsyn>,
                       @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?): Kvittering {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)

        return okEllerKastException { søknadService.mottaBarnetilsyn(søknad, vedleggData) }
    }

    @Deprecated("Bruk metode som henter vedlegg fra familie-dokument")
    @PostMapping(path = ["skolepenger"], consumes = [MULTIPART_FORM_DATA_VALUE])
    fun skolepengerOld(@RequestPart("søknad") søknad: SøknadMedVedlegg<SøknadSkolepenger>,
                       @RequestPart("vedlegg", required = false) vedleggListe: List<MultipartFile>?): Kvittering {
        val vedleggData = vedleggData(vedleggListe)

        validerVedlegg(søknad.vedlegg, vedleggData)
        return okEllerKastException { søknadService.mottaSkolepenger(søknad, vedleggData) }
    }

    private fun okEllerKastException(producer: () -> Kvittering): Kvittering {
        try {
            return producer.invoke()
        } catch (e: Exception) {
            if (e.getRootCause()?.message == UPDATE_FEILMELDING) {
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

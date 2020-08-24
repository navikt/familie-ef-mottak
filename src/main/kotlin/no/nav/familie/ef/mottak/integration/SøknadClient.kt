package no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.config.SakConfig
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.client.MultipartBuilder
import no.nav.familie.kontrakter.ef.sak.SakRequest
import no.nav.familie.kontrakter.ef.sak.Skjemasak
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.DefaultUriBuilderFactory
import java.net.URI

@Service
class SøknadClient(@Qualifier("restTemplateAzure") operations: RestOperations,
                   sakConfig: SakConfig) : AbstractRestClient(operations, "sak") {

    private val sendInnOvergangsstønadSakUri = urlBuilder(sakConfig).path(PATH_OVERGANGSSTØNAD).build()
    private val sendInnBarnetilsynSakUri = urlBuilder(sakConfig).path(PATH_BARNETILSYN).build()
    private val sendInnSkjemasakUri = urlBuilder(sakConfig).path(PATH_ARBEIDSSØKER).build()


    fun sendOvergangsstønad(sak: SakRequest<SøknadOvergangsstønad>, vedlegg: Map<String, ByteArray>): HttpStatus? {
        return send(sendInnOvergangsstønadSakUri, sak, vedlegg)
    }

    fun sendBarnetilsyn(sak: SakRequest<SøknadBarnetilsyn>, vedlegg: Map<String, ByteArray>): HttpStatus? {
        return send(sendInnBarnetilsynSakUri, sak, vedlegg)
    }

    private fun <T> send(uri: URI,
                         sak: SakRequest<T>,
                         vedlegg: Map<String, ByteArray>): HttpStatus {
        log.info("Sender søknad til {}", uri)
        try {
            val multipartBuilder = MultipartBuilder().withJson("sak", sak)
            vedlegg.forEach { multipartBuilder.withByteArray("vedlegg", it.key, it.value) }
            return postForEntity(uri, multipartBuilder.build(), MultipartBuilder.MULTIPART_HEADERS)
        } catch (e: RestClientResponseException) {
            log.warn("Innsending til sak feilet. Responskode: {}, body: {}",
                     e.rawStatusCode,
                     e.responseBodyAsString)
            throw IllegalStateException("Innsending til sak feilet. Status: ${e.rawStatusCode}, body: ${e.responseBodyAsString}",
                                        e)
        } catch (e: RestClientException) {
            throw IllegalStateException("Innsending til sak feilet.", e)
        }
    }

    fun send(skjemasak: Skjemasak): HttpStatus? {
        log.info("Sender søknad til {}", sendInnSkjemasakUri)

        try {
            throw RuntimeException("Denne endepunkten finnes ikke ennå.")
            //return postForEntity(sendInnSkjemasakUri, skjemasak)
        } catch (e: RestClientResponseException) {
            log.warn("Innsending til sak feilet. Responskode: {}, body: {}",
                     e.rawStatusCode,
                     e.responseBodyAsString)
            throw IllegalStateException(
                    "Innsending til sak feilet. Status: ${e.rawStatusCode}, body: ${e.responseBodyAsString}", e)
        } catch (e: RestClientException) {
            throw IllegalStateException("Innsending til sak feilet.", e)
        }
    }

    private fun urlBuilder(sakConfig: SakConfig) = DefaultUriBuilderFactory().uriString(sakConfig.url)

    companion object {

        const val PATH_OVERGANGSSTØNAD = "external/sak/overgangsstonad"
        const val PATH_BARNETILSYN = "external/sak/barnetilsyn"
        const val PATH_ARBEIDSSØKER = "external/sak/arbeidssoker"
    }

}

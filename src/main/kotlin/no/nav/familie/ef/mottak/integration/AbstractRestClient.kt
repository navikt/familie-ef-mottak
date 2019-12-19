package no.nav.familie.ef.mottak.integration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestOperations
import java.net.URI

abstract class AbstractRestClient(protected val operations: RestOperations) {

    private val marker: Marker = MarkerFactory.getMarker("CONFIDENTIAL")
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    protected inline fun <reified T> getForEntity(uri: URI): T {
        val respons = operations.getForEntity(uri, T::class.java)
        return validerOgPakkUt(respons)
    }

    protected inline fun <reified T> postForEntity(uri: URI, payload: Any): T {
        val respons = operations.postForEntity(uri, payload, T::class.java)
        return validerOgPakkUt(respons)
    }

    protected fun <T> validerOgPakkUt(respons: ResponseEntity<T>): T {
        logger.trace(marker, "Respons: {}", respons)
        if (!respons.statusCode.is2xxSuccessful) {
            throw HttpServerErrorException(respons.statusCode)
        }
        return respons.body ?: error("Ingen body i response")
    }

    override fun toString(): String = this::class.simpleName + " [operations=" + operations + "]"
}

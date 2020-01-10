package no.nav.familie.ef.mottak.integration.rest

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.Timer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.*
import java.net.URI
import java.util.concurrent.TimeUnit

abstract class AbstractRestClient(val operations: RestOperations,
                                  metricsPrefix: String) {

    private val responstid: Timer = Metrics.timer("$metricsPrefix.tid")
    private val responsSuccess: Counter = Metrics.counter("$metricsPrefix.response", "status", "success")
    private val responsFailure: Counter = Metrics.counter("$metricsPrefix.response", "status", "failure")

    private val secureLogger = LoggerFactory.getLogger("secureLogger")
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    inline fun <reified T : Any> getForEntity(uri: URI): T {
        return executeMedMetrics(uri) { operations.getForEntity<T>(uri) }
    }

    inline fun <reified T> postForEntity(uri: URI, payload: Any): T {
        return executeMedMetrics(uri) { operations.exchange<T>(uri, HttpMethod.POST, HttpEntity(payload)) }
    }

    private fun <T> validerOgPakkUt(respons: ResponseEntity<T>, uri: URI): T {
        if (!respons.statusCode.is2xxSuccessful) {
            secureLogger.info("Kall mot $uri feilet:  ${respons.body}")
            log.info("Kall mot $uri feilet: ${respons.statusCode}")
            throw HttpServerErrorException(respons.statusCode, "", respons.body?.toString()?.toByteArray(), Charsets.UTF_8)
        }
        return respons.body!!
    }

    fun <T> executeMedMetrics(uri: URI, function: () -> ResponseEntity<T>): T {
        try {
            val startTime = System.nanoTime()
            val responseEntity = function.invoke()
            responstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            responsSuccess.increment()
            return validerOgPakkUt(responseEntity, uri)
        } catch (e: RestClientResponseException) {
            responsFailure.increment()
            throw e
        } catch (e: Exception) {
            responsFailure.increment()
            throw RuntimeException("Feil ved kall mot uri=$uri", e)
        }
    }

    override fun toString(): String = this::class.simpleName + " [operations=" + operations + "]"
}

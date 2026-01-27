package no.nav.familie.ef.mottak.api

import jakarta.servlet.http.HttpServletRequest
import no.nav.familie.kontrakter.felles.Ressurs
import org.apache.kafka.common.requests.ApiError
import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@Suppress("unused")
@ControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @ExceptionHandler(Throwable::class)
    fun handleThrowable(throwable: Throwable): ResponseEntity<Ressurs<String>> {
        secureLogger.error("En feil har oppstått", throwable)
        logger.error("En feil har oppstått: ${NestedExceptionUtils.getMostSpecificCause(throwable).javaClass.simpleName} ")

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Ressurs.failure("Uventet feil"))
    }

    @ExceptionHandler(ApiFeil::class)
    fun handleThrowable(e: ApiFeil): ResponseEntity<Ressurs<String>> =
        ResponseEntity
            .status(e.httpStatus)
            .body(Ressurs.failure(e.feil))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        e: IllegalArgumentException,
        request: HttpServletRequest,
    ): ResponseEntity<Ressurs<String>> {
        secureLogger.warn(e.message, e.stackTraceToString())

        return ResponseEntity
            .badRequest()
            .body(
                Ressurs.failure(e.message),
            )
    }
}

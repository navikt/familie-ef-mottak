package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.FeilDto
import org.slf4j.LoggerFactory
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
    fun handleThrowable(throwable: Throwable): ResponseEntity<FeilDto> {
        secureLogger.error("En feil har oppstått", throwable)
        logger.error("En feil har oppstått: ${throwable.javaClass.simpleName} ")

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FeilDto("Uventet feil"))
    }

}
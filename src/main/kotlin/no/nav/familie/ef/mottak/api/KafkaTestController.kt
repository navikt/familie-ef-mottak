package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KafkaTestController(private val producer: DittNavKafkaProducer) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/kafka")
    @Unprotected
    fun ping(): String {
        val eventId = UUID.randomUUID().toString()
        logger.info("Sender melding til bruker $eventId")
        producer.sendToKafka("21057822284", "hei", "21057822284-test", eventId)
        return "OK"
    }

}
package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(path = ["/api/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KafkaTestController(private val producer: DittNavKafkaProducer) {

    @GetMapping("/kafka")
    @Unprotected
    fun ping(): String {
        producer.sendToKafka("21057822284", "hei", "21057822284-test", UUID.randomUUID().toString())
        return "OK"
    }

}
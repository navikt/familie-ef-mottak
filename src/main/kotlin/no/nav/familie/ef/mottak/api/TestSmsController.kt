package no.nav.familie.ef.mottak.api

import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.familie.ef.mottak.config.EttersendingConfig
import no.nav.familie.ef.mottak.service.DittNavKafkaProducer
import no.nav.familie.ef.mottak.util.lagMeldingPåminnelseManglerDokumentasjonsbehov
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/send-sms-test"])
@ProtectedWithClaims(issuer = "azuread")
class TestSmsController(private val producer: DittNavKafkaProducer, private val ettersendingConfig: EttersendingConfig) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("{passord}")
    fun sendSmsTilPrivatPerson(@PathVariable passord: String) {
        val linkMelding = lagMeldingPåminnelseManglerDokumentasjonsbehov(ettersendingConfig.ettersendingUrl, "overgangsstønad")

        if (passord.startsWith("240196")) {
            producer.sendToKafka(
                passord,
                linkMelding.melding,
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                linkMelding.link,
                PreferertKanal.SMS
            )
            logger.info("Sendte sms til privatperson")
        } else {
            logger.info("Sendte ikke sms til privatperson")
        }
    }

}

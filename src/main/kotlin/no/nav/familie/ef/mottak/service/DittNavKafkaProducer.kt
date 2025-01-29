package no.nav.familie.ef.mottak.service

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.domain.PreferertKanal
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import no.nav.tms.varsel.action.EksternKanal
import no.nav.tms.varsel.action.Produsent
import no.nav.tms.varsel.action.Sensitivitet
import no.nav.tms.varsel.action.Tekst
import no.nav.tms.varsel.action.Varseltype
import no.nav.tms.varsel.builder.VarselActionBuilder
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

@Service
class DittNavKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${BRUKERNOTIFIKASJON_VARSEL_TOPIC}")
    val topic: String,
    @Value("\${NAIS_APP_NAME}")
    val applicationName: String,
    @Value("\${NAIS_NAMESPACE}")
    val namespace: String,
    @Value("\${NAIS_CLUSTER_NAME}")
    val cluster: String,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    fun sendBeskjedTilBruker(
        type: Varseltype,
        varselId: String,
        ident: String,
        melding: String,
        sensitivitet: Sensitivitet,
        link: String? = null,
        aktivFremTil: ZonedDateTime? = null,
        smsVarslingstekst: String? = null,
    ) {
        val varsel = VarselActionBuilder.opprett {
            this.ident = ident
            this.type = type
            this.varselId = varselId
            this.sensitivitet = sensitivitet
            this.link = link
            this.aktivFremTil = aktivFremTil

            tekst = Tekst(
                spraakkode = "nb",
                tekst = melding,
                default = true
            )

            eksternVarsling {
                if (!smsVarslingstekst.isNullOrBlank()) {
                    preferertKanal = EksternKanal.SMS
                    this.smsVarslingstekst = smsVarslingstekst
                }
            }

            produsent = Produsent(
                cluster = cluster,
                namespace = namespace,
                appnavn = applicationName,
            )
        }

        secureLogger.debug("Sending to Kafka topic: {}: {}", topic, varsel)

        runCatching {
            val producerRecord = ProducerRecord(topic, varselId, varsel)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Could not send varsel to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send varsel to Kafka varsel={}", varsel, it)
            throw RuntimeException(errorMessage)
        }
    }
}

package no.nav.familie.ef.mottak.service

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DittNavKafkaProducer(private val kafkaTemplate: KafkaTemplate<Nokkel, Beskjed>) {

    @Value("\${SRV_CREDENTIAL_USERNAME}")
    private lateinit var systembruker: String

    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String


    fun sendToKafka(fnr: String, melding: String, grupperingsnummer: String, eventId: String, link: String?) {
        val beskjed = lagBeskjed(fnr, grupperingsnummer, melding, link)

        secureLogger.debug("Sending to Kafka topic: {}: {}", topic, beskjed)
        runCatching {
            val producerRecord = ProducerRecord(topic, lagNøkkel(eventId), beskjed)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Could not send DittNav to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send DittNav to Kafka melding={}", beskjed, it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun lagBeskjed(fnr: String,
                           grupperingsnummer: String,
                           melding: String,
                           link: String?): Beskjed {
        return Beskjed(System.currentTimeMillis(),
                       null,
                       fnr,
                       grupperingsnummer,
                       melding,
                       link,
                       4)
    }

    private fun lagNøkkel(eventId: String): Nokkel = Nokkel(systembruker, eventId)

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }

}
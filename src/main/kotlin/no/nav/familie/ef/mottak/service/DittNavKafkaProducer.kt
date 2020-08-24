package no.nav.familie.ef.mottak.service

import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import no.nav.familie.kontrakter.felles.objectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class DittNavKafkaProducer(private val kafkaTemplate: KafkaTemplate<String, String>) {

    @Value("\${SRV_CREDENTIAL_USERNAME}")
    private lateinit var systembruker: String

    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String

    fun sendToKafka(fnr: String, melding: String, grupperingsnummer: String, eventId: String) {
        val beskjed = lagBeskjed(fnr, grupperingsnummer, melding)

        logger.debug("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nDittNav: {}", topic, beskjed)
        runCatching {
            val producerRecord = ProducerRecord(topic, lagNøkkel(eventId), beskjed)
            val response = kafkaTemplate.send(producerRecord).get()
            val recordMetadata = response.recordMetadata
            logger.info("Melding sent to Kafka." +
                         " partition=${recordMetadata.partition()}" +
                         " offset=${recordMetadata.offset()}" +
                         " key=${response.producerRecord.key()}")
        }.onFailure {
            val errorMessage = "Could not send DittNav to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send DittNav to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun lagBeskjed(fnr: String,
                           grupperingsnummer: String,
                           melding: String): String {
        val beskjed = Beskjed(System.currentTimeMillis(),
                              null,
                              fnr,
                              grupperingsnummer,
                              melding,
                              "https://www.vg.no", // TODO ???
                              2)
        return objectMapper.writeValueAsString(beskjed)
    }

    private fun lagNøkkel(eventId: String): String =
            objectMapper.writeValueAsString(Nokkel(systembruker, eventId))

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }

}
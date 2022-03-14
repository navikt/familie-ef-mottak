package no.nav.familie.ef.mottak.service

import no.nav.brukernotifikasjon.schemas.builders.BeskjedInputBuilder
import no.nav.brukernotifikasjon.schemas.builders.NokkelInputBuilder
import no.nav.brukernotifikasjon.schemas.input.BeskjedInput
import no.nav.brukernotifikasjon.schemas.input.NokkelInput
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

@Service
class DittNavKafkaProducer(private val kafkaTemplate: KafkaTemplate<NokkelInput, BeskjedInput>) {

    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String


    fun sendToKafka(fnr: String, melding: String, grupperingsnummer: String, eventId: String, link: String?) {
        val nokkel = lagNøkkel(fnr, grupperingsnummer, eventId)
        val beskjed = lagBeskjed(melding, link)

        secureLogger.debug("Sending to Kafka topic: {}: {}", topic, beskjed)
        runCatching {
            val producerRecord = ProducerRecord(topic, nokkel, beskjed)
            kafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Could not send DittNav to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send DittNav to Kafka melding={}", beskjed, it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun lagNøkkel(fnr: String, grupperingsId: String, eventId: String): NokkelInput =
            NokkelInputBuilder()
                    .withAppnavn("familie-ef-mottak")
                    .withNamespace("teamfamilie")
                    .withFodselsnummer(fnr)
                    .withGrupperingsId(grupperingsId)
                    .withEventId(eventId)
                    .build()

    private fun lagBeskjed(melding: String, link: String?): BeskjedInput {
        val builder = BeskjedInputBuilder()
                .withEksternVarsling(false)
                .withSikkerhetsnivaa(4)
                .withSynligFremTil(null)
                .withTekst(melding)
                .withTidspunkt(LocalDateTime.now(UTC))

        if (link != null) builder.withLink(URI.create(link).toURL())
        return builder.build()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }

}
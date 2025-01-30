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
    private val kafkaTemplate: KafkaTemplate<NokkelInput, BeskjedInput>,
    private val nyKafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${BRUKERNOTIFIKASJON_VARSEL_TOPIC}")
    val nyTopic: String,
    @Value("\${NAIS_APP_NAME}")
    val applicationName: String,
    @Value("\${NAIS_NAMESPACE}")
    val namespace: String,
    @Value("\${NAIS_CLUSTER_NAME}")
    val cluster: String,
) {
    @Value("\${KAFKA_TOPIC_DITTNAV}")
    private lateinit var topic: String

    fun sendToKafka(
        fnr: String,
        melding: String,
        grupperingsnummer: String,
        eventId: String,
        link: URL? = null,
        kanal: PreferertKanal? = null,
    ) {
        val nokkel = lagNøkkel(fnr, grupperingsnummer, eventId)
        val beskjed = lagBeskjed(melding, link, kanal)

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

    fun sendBeskjedTilBruker(
        personIdent: String,
        varselId: String,
        melding: String,
        link: String? = null,
        aktivFremTil: ZonedDateTime? = null,
        eksternKanal: EksternKanal? = null
    ) {
        val varsel = VarselActionBuilder.opprett {
            this.ident = personIdent
            this.varselId = varselId
            this.link = link
            this.aktivFremTil = aktivFremTil
            this.type = Varseltype.Beskjed
            this.sensitivitet = Sensitivitet.High

            this.tekst = Tekst(
                spraakkode = "nb",
                tekst = melding,
                default = true
            )

            this.eksternVarsling {
                this.preferertKanal = eksternKanal
            }

            this.produsent = Produsent(
                cluster = cluster,
                namespace = namespace,
                appnavn = applicationName,
            )
        }

        secureLogger.debug("Sending to Kafka topic: {}: {}", nyTopic, varsel)

        runCatching {
            val producerRecord = ProducerRecord(nyTopic, varselId, varsel)
            nyKafkaTemplate.send(producerRecord).get()
        }.onFailure {
            val errorMessage = "Could not send varsel to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send varsel to Kafka varsel={}", varsel, it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun lagNøkkel(
        fnr: String,
        grupperingsId: String,
        eventId: String,
    ): NokkelInput =
        NokkelInputBuilder()
            .withAppnavn("familie-ef-mottak")
            .withNamespace("teamfamilie")
            .withFodselsnummer(fnr)
            .withGrupperingsId(grupperingsId)
            .withEventId(eventId)
            .build()

    private fun lagBeskjed(
        melding: String,
        link: URL?,
        kanal: PreferertKanal?,
    ): BeskjedInput {
        val builder =
            BeskjedInputBuilder()
                .withSikkerhetsnivaa(4)
                .withSynligFremTil(null)
                .withTekst(melding)
                .withTidspunkt(LocalDateTime.now(UTC))

        if (link != null) builder.withLink(link)
        if (kanal != null) builder.withEksternVarsling(true).withPrefererteKanaler(kanal)

        return builder.build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}

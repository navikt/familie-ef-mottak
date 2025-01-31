package no.nav.familie.ef.mottak.service

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
    fun sendBeskjedTilBruker(
        personIdent: String,
        varselId: String,
        melding: String,
        link: String? = null,
        aktivFremTil: ZonedDateTime? = null,
        eksternKanal: EksternKanal? = null,
    ) {
        val varsel =
            VarselActionBuilder.opprett {
                this.ident = personIdent
                this.varselId = varselId
                this.link = link
                this.aktivFremTil = aktivFremTil
                this.type = Varseltype.Beskjed
                this.sensitivitet = Sensitivitet.High

                this.tekst =
                    Tekst(
                        spraakkode = "nb",
                        tekst = melding,
                        default = true,
                    )

                this.eksternVarsling {
                    this.preferertKanal = eksternKanal
                }

                this.produsent =
                    Produsent(
                        cluster = cluster,
                        namespace = namespace,
                        appnavn = applicationName,
                    )
            }

        secureLogger.info("Sender til Kafka topic: {}: {}", topic, varsel)

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

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}

package no.nav.familie.ef.mottak.config

import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig
import no.nav.brukernotifikasjon.schemas.Beskjed
import no.nav.brukernotifikasjon.schemas.Nokkel
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.LoggingProducerListener

/**
 * Config for å nå brukernotifikasjon topic som fortsatt er on-prem. Denne klassen kan fjernes når topic er klar i aiven i prod.

@EnableKafka
@Configuration
class DittNavKafkaConfig(
    @Value("\${KAFKA_ONPREM_BOOTSTRAP_SERVERS}")
    private val bootstrapServers: String,
    @Value("\${KAFKA_ONPREM_SCHEMA_REGISTRY_URL}")
    private val schemaRegistryUrl: String,
    @Value("\${SERVICE_USER_USERNAME}")
    private val username: String,
    @Value("\${SERVICE_USER_PASSWORD}")
    private val password: String
) {

    @Bean
    fun producerConfigs(): Map<String, Any> {
        val props: MutableMap<String, Any> = HashMap()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.CLIENT_ID_CONFIG] = username
        props[KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
        props[SaslConfigs.SASL_JAAS_CONFIG] =
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        props[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"

        return props
    }

    @Bean
    fun producerFactory(): ProducerFactory<Nokkel, Beskjed> {
        return DefaultKafkaProducerFactory(producerConfigs())
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<Nokkel, Beskjed> {
        return KafkaTemplate(producerFactory()).apply {
            val producerListener = LoggingProducerListener<String, String>()
            producerListener.setIncludeContents(false)
        }
    }
}
 */
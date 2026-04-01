package no.nav.familie.ef.mottak.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration
import kotlin.collections.set

@EnableKafka
@Configuration
class KafkaConfig {
    @Bean
    fun kafkaJournalføringHendelseListenerContainerFactory(
        properties: KafkaProperties,
        kafkaErrorHandler: KafkaErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord> =
        ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord>().apply {
            val props = properties.buildConsumerProperties()
            props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1
            props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false

            props[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true
            props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
            props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = KafkaAvroDeserializer::class.java

            val consumerFactory = DefaultKafkaConsumerFactory<Long, JournalfoeringHendelseRecord>(props)
            setConsumerFactory(consumerFactory)

            setCommonErrorHandler(kafkaErrorHandler)

            setContainerCustomizer {
                it.containerProperties.apply {
                    ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
                    setAuthExceptionRetryInterval(Duration.ofSeconds(5))
                }
            }
        }
}

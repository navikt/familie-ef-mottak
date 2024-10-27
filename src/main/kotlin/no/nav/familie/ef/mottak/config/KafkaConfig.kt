package no.nav.familie.ef.mottak.config

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration

@EnableKafka
@Configuration
class KafkaConfig {
    @Bean
    fun kafkaJournalføringHendelseListenerContainerFactory(
        properties: KafkaProperties,
        kafkaErrorHandler: KafkaErrorHandler,
        sslBundles: ObjectProvider<SslBundles>,
    ): ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord> {
        val consumerProperties = properties.buildConsumerProperties(sslBundles.getIfAvailable())
        consumerProperties[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1
        consumerProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        consumerProperties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        consumerProperties[KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG] = true

        val factory = ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord>()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
        factory.containerProperties.authExceptionRetryInterval = Duration.ofSeconds(2)
        factory.consumerFactory = DefaultKafkaConsumerFactory(consumerProperties)
        factory.setCommonErrorHandler(kafkaErrorHandler)
        return factory
    }
}

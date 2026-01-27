package no.nav.familie.ef.mottak.config

import no.nav.familie.kafka.KafkaErrorHandler
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties

@EnableKafka
@Configuration
class KafkaConfig {
    @Bean
    fun kafkaJournalføringHendelseListenerContainerFactory(
        properties: KafkaProperties,
        kafkaErrorHandler: KafkaErrorHandler,
    ): ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord> =
        ConcurrentKafkaListenerContainerFactory<Long, JournalfoeringHendelseRecord>().apply {
            setConsumerFactory(
                DefaultKafkaConsumerFactory(properties.buildConsumerProperties()),
            )

            setCommonErrorHandler(kafkaErrorHandler)

            setContainerCustomizer {
                it.containerProperties.apply {
                    ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE
                    // authExceptionRetryInterval = Duration.ofSeconds(2) TODO legges inn i Error handler
                }
            }
        }
}

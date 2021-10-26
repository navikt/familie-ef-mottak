package no.nav.familie.ef.mottak.hendelse

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service


@Service
class JournalhendelseKafkaListener(val kafkaHåndterer: JournalhendelseKafkaHåndterer,
                                   private val featureToggleService: FeatureToggleService) {

    @KafkaListener(id = "familie-ef-mottak",
                   topics = ["\${JOURNALFOERINGHENDELSE_V1_TOPIC_URL}"],
                   containerFactory = "kafkaJournalføringHendelseListenerContainerFactory",
                   idIsGroup = false)
    fun listen(consumerRecord: ConsumerRecord<Long, JournalfoeringHendelseRecord>, ack: Acknowledgment) {
        if (featureToggleService.isEnabled("familie.ef.mottak.kafka.gcp")) {
            kafkaHåndterer.håndterHendelse(consumerRecord, ack)
        } else {
            throw Exception("Lytting til topic er skrudd av som følge av migrering til gcp")
        }
    }
}
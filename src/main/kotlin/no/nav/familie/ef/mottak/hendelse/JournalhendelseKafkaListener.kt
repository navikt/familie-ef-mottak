package no.nav.familie.ef.mottak.hendelse

import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class JournalhendelseKafkaListener(val kafkaHåndterer: JournalhendelseKafkaHåndterer) {

    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(
        id = "familie-ef-mottak",
        topics = ["\${JOURNALFOERINGHENDELSE_V1_TOPIC_URL}"],
        containerFactory = "kafkaJournalføringHendelseListenerContainerFactory",
        idIsGroup = false,
        groupId = "srvfamilie-ef-mot"
    )
    fun listen(consumerRecord: ConsumerRecord<String, JournalfoeringHendelseRecord>, ack: Acknowledgment) {
        kafkaHåndterer.håndterHendelse(consumerRecord, ack)
    }
}

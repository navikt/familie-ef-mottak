package no.nav.familie.ef.mottak.hendelse

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service


@Service
class JournalhendelseKafkaListener(val kafkaHåndterer: JournalhendelseKafkaHåndterer,
                                   private val featureToggleService: FeatureToggleService,
                                   private val hendelsesloggRepository: HendelsesloggRepository) {

    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    @KafkaListener(id = "familie-ef-mottak",
                   topics = ["\${JOURNALFOERINGHENDELSE_V1_TOPIC_URL}"],
                   containerFactory = "kafkaJournalføringHendelseListenerContainerFactory",
                   idIsGroup = false,
                   groupId = "srvfamilie-ef-mot")
    fun listen(consumerRecord: ConsumerRecord<Long, JournalfoeringHendelseRecord>, ack: Acknowledgment) {
        if (hendelsesloggRepository.hentMaxOffset() < consumerRecord.offset()) {
            kafkaHåndterer.håndterHendelse(consumerRecord, ack)
        }
    }
}
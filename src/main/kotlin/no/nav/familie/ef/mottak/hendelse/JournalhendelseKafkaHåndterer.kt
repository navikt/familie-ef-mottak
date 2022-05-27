package no.nav.familie.ef.mottak.hendelse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.log.IdUtils
import no.nav.familie.log.mdc.MDCConstants
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class JournalhendelseKafkaHåndterer(val journalhendelseService: JournalhendelseService) {

    val feilCounter: Counter = Metrics.counter("alene.med.barn.journalhendelse.feilet")
    val logger: Logger = LoggerFactory.getLogger(JournalhendelseKafkaListener::class.java)

    fun håndterHendelse(consumerRecord: ConsumerRecord<String, JournalfoeringHendelseRecord>, ack: Acknowledgment) {
        try {
            val hendelseRecord = consumerRecord.value()
            val callId = hendelseRecord.kanalReferanseId.toStringOrNull() ?: IdUtils.generateId()
            MDC.put(MDCConstants.MDC_CALL_ID, callId)
            if (hendelseRecord.skalProsesseres()) {
                journalhendelseService.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())
            }
            ack.acknowledge()
        } catch (e: Exception) {
            logger.error("Feil ved prosessering av journalhendelser ", e)
            feilCounter.increment()
            throw e
        } finally {
            MDC.clear()
        }
    }

    fun CharSequence.toStringOrNull(): String? {
        return if (this.isNotBlank()) this.toString() else null
    }
}

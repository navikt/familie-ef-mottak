package no.nav.familie.ef.mottak.config

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.kafka.listener.ContainerStoppingErrorHandler
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * hentet fra pensr
 */
@Component
class KafkaErrorHandler : ContainerStoppingErrorHandler() {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)
    val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")

    private val executor: Executor
    private val counter = AtomicInteger(0)
    private val lastError = AtomicLong(0)
    override fun handle(e: Exception,
                        records: List<ConsumerRecord<*, *>>?,
                        consumer: Consumer<*, *>,
                        container: MessageListenerContainer) {
        Thread.sleep(1000)

        if (records.isNullOrEmpty()) {
            logger.error("Feil ved konsumering av melding. Ingen records. ${consumer.subscription()}", e)
            scheduleRestart(e, records, consumer, container, "Ukjent topic")
        } else {
            records.first().run {
                logger.error("Feil ved konsumering av melding fra ${this.topic()}. id ${this.key()}, " +
                             "offset: ${this.offset()}, partition: ${this.partition()}")
                secureLogger.error("${this.topic()} - Problemer med prosessering av $records", e)
                scheduleRestart(e, records, consumer, container, this.topic())
            }
        }
    }

    private fun scheduleRestart(e: Exception,
                                records: List<ConsumerRecord<*, *>>? = null,
                                consumer: Consumer<*, *>,
                                container: MessageListenerContainer,
                                topic: String) {
        val now = System.currentTimeMillis()
        if (now - lastError.getAndSet(now) > COUNTER_RESET_TIME) {
            counter.set(0)
        }
        val numErrors = counter.incrementAndGet()
        val stopTime =
                if (numErrors > SLOW_ERROR_COUNT) LONG_TIME else SHORT_TIME * numErrors
        executor.execute {
            try {
                Thread.sleep(stopTime)
                logger.warn("Starter kafka container for {}", topic)
                container.start()
            } catch (exception: Exception) {
                logger.error("Feil oppstod ved venting og oppstart av kafka container", exception)
            }
        }
        logger.warn("Stopper kafka container for {} i {}", topic, Duration.ofMillis(stopTime).toString())
        super.handle(e, records, consumer, container)
    }

    companion object {
        private val LONG_TIME = Duration.ofHours(3).toMillis()
        private val SHORT_TIME = Duration.ofSeconds(20).toMillis()
        private const val SLOW_ERROR_COUNT = 10
        private val COUNTER_RESET_TIME = SHORT_TIME * SLOW_ERROR_COUNT * 2
    }

    init {
        this.executor = SimpleAsyncTaskExecutor()
    }

}
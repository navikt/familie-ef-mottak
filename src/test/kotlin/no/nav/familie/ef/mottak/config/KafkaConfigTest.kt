package no.nav.familie.ef.mottak.config

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.mockk.mockk
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_PAPIRSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.journalføringHendelseRecord
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.avro.generic.GenericData
import org.junit.jupiter.api.Test
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KafkaConfigTest {
    @Test
    fun `deserialiserer journalhendelser med appens Kafka-konfigurasjon`() {
        val props = requireNotNull(opprettKafkaFactory().consumerFactory).configurationProperties
        val schemaRegistry = MockSchemaRegistryClient()
        val hendelse = journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD)
        val writerRecord =
            GenericData.Record(hendelse.schema).apply {
                hendelse.schema.fields.forEach { put(it.name(), hendelse[it.pos()]) }
            }

        KafkaAvroSerializer(schemaRegistry).use { serializer ->
            KafkaAvroDeserializer(schemaRegistry).use { deserializer ->
                serializer.configure(props, false)
                deserializer.configure(props, false)

                val bytes = serializer.serialize("journalhendelser-test", writerRecord)
                val resultat = deserializer.deserialize("journalhendelser-test", bytes)

                assertIs<JournalfoeringHendelseRecord>(resultat)
                assertEquals(hendelse, resultat)
            }
        }
    }

    private fun opprettKafkaFactory() =
        KafkaConfig().kafkaJournalføringHendelseListenerContainerFactory(
            KafkaProperties().apply {
                properties["schema.registry.url"] = "mock://journalhendelser-test"
            },
            mockk(),
        )
}

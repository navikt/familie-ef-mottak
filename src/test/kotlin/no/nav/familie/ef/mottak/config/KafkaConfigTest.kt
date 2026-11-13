package no.nav.familie.ef.mottak.config

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.mockk.mockk
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_PAPIRSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.journalføringHendelseRecord
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.avro.generic.GenericData
import org.apache.avro.util.ClassUtils
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class KafkaConfigTest {
    @Test
    fun `har eksplisitt pakkegodkjenning ved testoppstart`() {
        assertEquals(
            "no.nav.joarkjournalfoeringhendelser",
            System.getProperty("org.apache.avro.SERIALIZABLE_PACKAGES"),
        )
    }

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

    @Test
    fun `beholder standardklasser og avviser ikke-godkjente klasser med Avros klassekontroll`() {
        assumeTrue(
            ClassUtils::class.java.getResource("ClassSecurityValidator.class") != null,
            "Avro-versjonen har ikke klassekontrollen som ble innført i 1.12.2",
        )

        assertEquals(String::class.java, ClassUtils.forName(String::class.java.name))

        val exception =
            assertThrows<SecurityException> {
                ClassUtils.forName(KafkaConfigTest::class.java.name)
            }
        assertContains(assertNotNull(exception.message), KafkaConfigTest::class.java.name)
    }

    private fun opprettKafkaFactory() =
        KafkaConfig().kafkaJournalføringHendelseListenerContainerFactory(
            KafkaProperties().apply {
                properties["schema.registry.url"] = "mock://journalhendelser-test"
            },
            mockk(),
        )
}

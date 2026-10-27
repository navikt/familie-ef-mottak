package no.nav.familie.ef.mottak.config

import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroSerializer
import io.mockk.mockk
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_PAPIRSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.journalføringHendelseRecord
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.avro.generic.GenericData
import org.apache.avro.util.ClassSecurityValidator
import org.apache.avro.util.ClassUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

@Isolated("Endrer Avros globale klassekontroll")
class KafkaConfigTest {
    private lateinit var opprinneligKlassekontroll: ClassSecurityValidator.ClassSecurityPredicate

    @BeforeEach
    fun setUp() {
        opprinneligKlassekontroll = ClassSecurityValidator.getGlobal()
        ClassSecurityValidator.setGlobal(ClassSecurityValidator.DEFAULT_TRUSTED_CLASSES)
    }

    @AfterEach
    fun tearDown() {
        ClassSecurityValidator.setGlobal(opprinneligKlassekontroll)
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
    fun `godkjenner bare hendelsesklassen og beholder standardkontrollen`() {
        opprettKafkaFactory()

        assertEquals(
            JournalfoeringHendelseRecord::class.java,
            ClassUtils.forName(JournalfoeringHendelseRecord::class.java.name),
        )
        assertEquals(String::class.java, ClassUtils.forName(String::class.java.name))
        assertFailsWith<SecurityException> {
            ClassUtils.forName(JournalfoeringHendelseRecord.Builder::class.java.name)
        }
        assertFailsWith<SecurityException> {
            ClassUtils.forName(File::class.java.name)
        }
    }

    @Test
    fun `beholder klasser som allerede er godkjent`() {
        ClassSecurityValidator.setGlobal(
            ClassSecurityValidator.composite(
                ClassSecurityValidator.getGlobal(),
                ClassSecurityValidator.builder().add(KafkaConfig::class.java).build(),
            ),
        )

        opprettKafkaFactory()

        assertEquals(KafkaConfig::class.java, ClassUtils.forName(KafkaConfig::class.java.name))
    }

    private fun opprettKafkaFactory() =
        KafkaConfig().kafkaJournalføringHendelseListenerContainerFactory(
            KafkaProperties().apply {
                properties["schema.registry.url"] = "mock://journalhendelser-test"
            },
            mockk(),
        )
}

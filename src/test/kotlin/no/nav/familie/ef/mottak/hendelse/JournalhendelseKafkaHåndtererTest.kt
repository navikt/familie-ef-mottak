package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.hendelse

import io.mockk.*
import io.mockk.impl.annotations.MockK
import no.nav.familie.ef.mottak.hendelse.JournalhendelseServiceTest
import no.nav.familie.ef.mottak.hendelse.JournalhendelseKafkaHåndterer
import no.nav.familie.ef.mottak.hendelse.JournalhendelseService
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.kafka.support.Acknowledgment
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JournalhendelseKafkaHåndtererTest {

    @MockK
    lateinit var journalhendelseServiceMock: JournalhendelseService

    @MockK(relaxed = true)
    lateinit var ack: Acknowledgment

    lateinit var journalhendelseKafkaHåndterer: JournalhendelseKafkaHåndterer

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        journalhendelseKafkaHåndterer = JournalhendelseKafkaHåndterer(journalhendelseServiceMock)
        clearAllMocks()
    }

    @Test
    fun `Ikke gyldige hendelsetyper skal ignoreres`() {
        val ugyldigHendelsetypeRecord = opprettRecord(
            journalpostId = JournalhendelseServiceTest.JOURNALPOST_PAPIRSØKNAD,
            hendelseType = "UgyldigType"
        )
        val consumerRecord = ConsumerRecord(
            "topic", 1,
            JournalhendelseServiceTest.OFFSET,
            42L, ugyldigHendelsetypeRecord
        )

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 0) {
            journalhendelseServiceMock.prosesserNyHendelse(any(), any())
        }

    }

    @Test
    fun `Hendelser hvor journalpost ikke har tema ENF skal ignoreres`() {
        val ukjentTemaRecord = opprettRecord(journalpostId = JournalhendelseServiceTest.JOURNALPOST_PAPIRSØKNAD, temaNytt = "UKJ")

        val consumerRecord = ConsumerRecord("topic", 1,
            JournalhendelseServiceTest.OFFSET,
            42L, ukjentTemaRecord)

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 0) {
            journalhendelseServiceMock.prosesserNyHendelse(any(), any())
        }

    }


    @Test
    fun `kast unntak for prosesserhendelse, forvent not acknowledged`() {
        val consumerRecord = ConsumerRecord(
            "topic", 1,
            JournalhendelseServiceTest.OFFSET,
            42L, opprettRecord(JournalhendelseServiceTest.JOURNALPOST_PAPIRSØKNAD)
        )
        every {
            journalhendelseServiceMock.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())
        } throws Exception("Unntak")

        assertFailsWith(Exception::class) {
            journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)
        }

        verify(exactly = 0) {
            ack.acknowledge()
        }
    }

    @Test
    fun `send inn gyldig consumer record, forvent acknowledged`() {
        val consumerRecord = ConsumerRecord(
            "topic", 1,
            JournalhendelseServiceTest.OFFSET,
            42L, opprettRecord(JournalhendelseServiceTest.JOURNALPOST_PAPIRSØKNAD)
        )

        every {
            journalhendelseServiceMock.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())
        } just Runs

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 1) {
            ack.acknowledge()
        }
    }

    private fun opprettRecord(
        journalpostId: String,
        hendelseType: String = "MidlertidigJournalført",
        temaNytt: String = "ENF"
    ): JournalfoeringHendelseRecord {
        return JournalfoeringHendelseRecord(
            JournalhendelseServiceTest.HENDELSE_ID,
            1,
            hendelseType,
            journalpostId.toLong(),
            "M",
            "ENF",
            temaNytt,
            "SKAN_NETS",
            "kanalReferanseId",
            "ENF"
        )
    }
}
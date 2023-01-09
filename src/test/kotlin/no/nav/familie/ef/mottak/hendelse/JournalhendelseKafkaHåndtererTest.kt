package no.nav.familie.ef.mottak.hendelse

import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import no.nav.familie.ef.mottak.mockapi.clearAllMocks
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_PAPIRSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.OFFSET
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.journalføringHendelseRecord
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

    private lateinit var journalhendelseKafkaHåndterer: JournalhendelseKafkaHåndterer

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        journalhendelseKafkaHåndterer = JournalhendelseKafkaHåndterer(journalhendelseServiceMock)
        clearAllMocks(this)
    }

    @Test
    fun `Ikke gyldige hendelsetyper skal ignoreres`() {
        val ugyldigHendelsetypeRecord = journalføringHendelseRecord(
            JOURNALPOST_PAPIRSØKNAD,
            hendelseType = "UgyldigType",
        )
        val consumerRecord = ConsumerRecord(
            "topic",
            1,
            OFFSET,
            "42",
            ugyldigHendelsetypeRecord,
        )

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 0) {
            journalhendelseServiceMock.prosesserNyHendelse(any(), any())
        }
    }

    @Test
    fun `Hendelser hvor journalpost ikke har tema ENF skal ignoreres`() {
        val ukjentTemaRecord = journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD, temaNytt = "UKJ")

        val consumerRecord = ConsumerRecord(
            "topic",
            1,
            OFFSET,
            "42",
            ukjentTemaRecord,
        )

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 0) {
            journalhendelseServiceMock.prosesserNyHendelse(any(), any())
        }
    }

    @Test
    fun `kast unntak for prosesserhendelse, forvent not acknowledged`() {
        val consumerRecord = ConsumerRecord(
            "topic",
            1,
            OFFSET,
            "42",
            journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD),
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
            "topic",
            1,
            OFFSET,
            "42",
            journalføringHendelseRecord(JOURNALPOST_PAPIRSØKNAD),
        )

        every {
            journalhendelseServiceMock.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())
        } just Runs

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 1) {
            ack.acknowledge()
        }
    }
}

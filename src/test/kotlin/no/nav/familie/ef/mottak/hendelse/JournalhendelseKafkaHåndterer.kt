package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.hendelse

import io.mockk.*
import io.mockk.impl.annotations.MockK
import no.nav.familie.ef.mottak.hendelse.JournalføringHendelseServiceTest
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
class JournalføringHendelseServiceTest {

    @MockK
    lateinit var journalHendelsesServiceMock: JournalhendelseService

    @MockK(relaxed = true)
    lateinit var ack: Acknowledgment

    lateinit var journalhendelseKafkaHåndterer: JournalhendelseKafkaHåndterer

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        journalhendelseKafkaHåndterer = JournalhendelseKafkaHåndterer(journalHendelsesServiceMock)
        clearAllMocks()
    }

    @Test
    fun `kast unntak for prosesserhendelse, forvent not acknowledged`() {
        val consumerRecord = ConsumerRecord("topic", 1,
                                            JournalføringHendelseServiceTest.OFFSET,
                                            42L, opprettRecord(JournalføringHendelseServiceTest.JOURNALPOST_PAPIRSØKNAD))
        every {
            journalHendelsesServiceMock.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())
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
        val consumerRecord = ConsumerRecord("topic", 1,
                                            JournalføringHendelseServiceTest.OFFSET,
                                            42L, opprettRecord(JournalføringHendelseServiceTest.JOURNALPOST_PAPIRSØKNAD))

        every {
            journalHendelsesServiceMock.prosesserNyHendelse(consumerRecord.value(), consumerRecord.offset())
        } just Runs

        journalhendelseKafkaHåndterer.håndterHendelse(consumerRecord, ack)

        verify(exactly = 1) {
            ack.acknowledge()
        }
    }

    private fun opprettRecord(journalpostId: String,
                              hendelseType: String = "MidlertidigJournalført",
                              temaNytt: String = "ENF"): JournalfoeringHendelseRecord {
        return JournalfoeringHendelseRecord(JournalføringHendelseServiceTest.HENDELSE_ID,
                                            1,
                                            hendelseType,
                                            journalpostId.toLong(),
                                            "M",
                                            "ENF",
                                            temaNytt,
                                            "SKAN_NETS",
                                            "kanalReferanseId",
                                            "ENF")
    }
}
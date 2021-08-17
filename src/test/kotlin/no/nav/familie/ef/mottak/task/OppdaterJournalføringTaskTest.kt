package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_DIGITALSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.ef.mottak.service.FAGOMRÅDE_ENSLIG_FORSØRGER
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.task.FerdigstillJournalføringTask
import no.nav.familie.ef.mottak.task.OppdaterJournalføringTask
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.kontrakter.felles.dokarkiv.Sak
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

internal class OppdaterJournalføringTaskTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val arkiveringService: ArkiveringService = ArkiveringService(integrasjonerClient, søknadService, mockk(), mockk(), mockk())
    private val taskRepository: TaskRepository = mockk()
    private val oppdaterJournalføringTask = OppdaterJournalføringTask(taskRepository, arkiveringService)

    @Test
    internal fun `skal ferdigstille journalføring med riktig id og enhet`() {
        val saksnummer = "A01"
        val infotrygdSaksnummer = "0305A01"
        val journalpostIdSlot = slot<String>()
        val oppdaterJournalpostRequestSlot = slot<OppdaterJournalpostRequest>()

        every {
            integrasjonerClient.finnInfotrygdSaksnummerForSak(saksnummer, FAGOMRÅDE_ENSLIG_FORSØRGER, any())
        } returns infotrygdSaksnummer

        every {
            søknadService.get("123")
        } returns søknad(saksnummer = saksnummer,
                         journalpostId = JOURNALPOST_DIGITALSØKNAD)

        every {
            integrasjonerClient.oppdaterJournalpost(capture(oppdaterJournalpostRequestSlot), capture(journalpostIdSlot))
        } returns OppdaterJournalpostResponse("123")

        every {
            integrasjonerClient.hentJournalpost(any())
        } returns Journalpost(journalpostId = JOURNALPOST_DIGITALSØKNAD,
                              journalposttype = Journalposttype.I,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "ENF",
                              kanal = "NAV_NO",
                              behandlingstema = null,
                              dokumenter = null,
                              journalforendeEnhet = null,
                              sak = null)


        oppdaterJournalføringTask.doTask(Task(type = "", payload = "123", properties = Properties()))

        assertThat(journalpostIdSlot.captured).isEqualTo(JOURNALPOST_DIGITALSØKNAD)
        assertThat(oppdaterJournalpostRequestSlot.captured.sak).isEqualTo(Sak(fagsakId = infotrygdSaksnummer,
                                                                              fagsaksystem = Fagsystem.IT01,
                                                                              sakstype = "FAGSAK"))
        assertThat(oppdaterJournalpostRequestSlot.captured.bruker!!.id).isEqualTo("123456789012")
        assertThat(oppdaterJournalpostRequestSlot.captured.bruker!!.idType).isEqualTo(BrukerIdType.AKTOERID)
    }

    @Test
    fun `Skal gå til FerdigstillOppgaveTask når FerdigstillJournalføringTask er utført`() {
        val slot = slot<Task>()
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        oppdaterJournalføringTask.onCompletion(Task(type = "", payload = "", properties = Properties()))

        Assertions.assertEquals(FerdigstillJournalføringTask.TYPE, slot.captured.type)
    }

}
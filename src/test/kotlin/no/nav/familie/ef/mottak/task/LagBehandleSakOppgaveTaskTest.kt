package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.hendelse.JournalføringHendelseServiceTest
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*
import org.assertj.core.api.Assertions.assertThat as assertThat

internal class LagBehandleSakOppgaveTaskTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val oppgaveService: OppgaveService = mockk()
    private val lagBehandleSakOppgaveTask = LagBehandleSakOppgaveTask(oppgaveService, søknadService, integrasjonerClient)

    @Test
    internal fun `skal lage oppgave som skal saksbehandles`() {
        val saksnummer="12345"
        val saksnummerSlot = slot<String>()

        every {
            søknadService.get("123L")
        } returns Soknad(søknadJson = "",
                         dokumenttype = "noe",
                         saksnummer = saksnummer,
                         journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                         fnr = FnrGenerator.generer())

        every {
            integrasjonerClient.hentJournalpost(any())
        } returns Journalpost(journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                              journalposttype = Journalposttype.I,
                              journalstatus = Journalstatus.MOTTATT,
                              bruker = Bruker("123456789012", BrukerIdType.AKTOERID),
                              tema = "ENF",
                              kanal = "NAV_NO",
                              behandlingstema = null,
                              dokumenter = null,
                              journalforendeEnhet = null,
                              sak = null)

        every {
            oppgaveService.lagBehandleSakOppgave(any(), capture(saksnummerSlot))
        } returns 1L

        lagBehandleSakOppgaveTask.doTask(Task.nyTask(type = "", payload = "123L", properties = Properties()))
        assertThat(saksnummerSlot.captured).isEqualTo(saksnummer)
    }

}
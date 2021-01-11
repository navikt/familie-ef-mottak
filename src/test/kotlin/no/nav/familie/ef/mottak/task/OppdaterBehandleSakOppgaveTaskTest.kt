package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.FAGOMRÅDE_ENSLIG_FORSØRGER
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class OppdaterBehandleSakOppgaveTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val oppgaveService: OppgaveService = mockk(relaxed = true)
    private val integrasjonerClient: IntegrasjonerClient = mockk(relaxed = true)
    private val søknadService: SøknadService = mockk(relaxed = true)

    private val oppdaterBehandleSakOppgaveTask =
            OppdaterBehandleSakOppgaveTask(oppgaveService, søknadService, integrasjonerClient, taskRepository)

    @Test
    internal fun `Skal oppdatere behandle-sak-oppgave med saksnummer fra infotrygd`() {
        val properties = Properties()
        val oppgaveId = 12L
        properties.put(LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey, oppgaveId)
        val saksblokk = "A01"
        val saksnummer = "12345A01"
        val saksblokkSlot = slot<String>()
        val saksnummerSlot = slot<String>()
        val soknad = Soknad(id = UUID.randomUUID().toString(),
                            fnr = "12345678901",
                            søknadJson = "",
                            dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                            saksnummer = saksblokk)
        every { søknadService.get(any()) } returns soknad
        every {
            integrasjonerClient.finnInfotrygdSaksnummerForSak(saksblokk,
                                                              FAGOMRÅDE_ENSLIG_FORSØRGER,
                                                              any())
        } returns saksnummer
        every { oppgaveService.oppdaterOppgave(oppgaveId, capture(saksblokkSlot), capture(saksnummerSlot)) } returns oppgaveId
        oppdaterBehandleSakOppgaveTask.doTask(Task(type = "", payload = "", properties = properties))
        assertThat(saksblokkSlot.captured).isEqualTo(saksblokk)
        assertThat(saksnummerSlot.captured).isEqualTo(saksnummer)
    }

}
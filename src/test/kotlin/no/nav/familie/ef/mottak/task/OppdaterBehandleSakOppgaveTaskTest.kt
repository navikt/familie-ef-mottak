package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.service.FAGOMRÅDE_ENSLIG_FORSØRGER
import no.nav.familie.ef.mottak.service.OppgaveService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Properties

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
        properties[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] = oppgaveId.toString()
        val saksblokk = "A01"
        val saksnummer = "12345A01"
        val saksblokkSlot = slot<String>()
        val saksnummerSlot = slot<String>()
        val søknad = søknad(dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                            saksnummer = saksblokk)
        every { søknadService.get(any()) } returns søknad
        every {
            integrasjonerClient.finnInfotrygdSaksnummerForSak(saksblokk,
                                                              FAGOMRÅDE_ENSLIG_FORSØRGER,
                                                              any())
        } returns saksnummer
        every {
            oppgaveService.settSaksnummerPåInfotrygdOppgave(oppgaveId,
                                                            capture(saksblokkSlot),
                                                            capture(saksnummerSlot))
        } returns oppgaveId
        oppdaterBehandleSakOppgaveTask.doTask(Task(type = "", payload = "", properties = properties))
        assertThat(saksblokkSlot.captured).isEqualTo(saksblokk)
        assertThat(saksnummerSlot.captured).isEqualTo(saksnummer)
    }
}
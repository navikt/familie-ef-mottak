package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.hendelse.JournalføringHendelseServiceTest
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.ef.mottak.task.HentSaksnummerFraJoarkTask
import no.nav.familie.ef.mottak.task.LagBehandleSakOppgaveTask
import no.nav.familie.ef.mottak.task.OppdaterBehandleSakOppgaveTask
import no.nav.familie.ef.mottak.task.OpprettSakTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.*

internal class OpprettSakTaskTest {

    private val sakService: SakService = mockk()
    private val soknadRepository: SoknadRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val opprettSakTask = OpprettSakTask(taskRepository, sakService, soknadRepository)

    private val saksnummer = "A01"
    private val soknad = Soknad(søknadJson = "",
                                dokumenttype = "noe",
                                saksnummer = saksnummer,
                                journalpostId = JournalføringHendelseServiceTest.JOURNALPOST_DIGITALSØKNAD,
                                fnr = FnrGenerator.generer())

    @Test
    internal fun `skal opprette sak og persistere saksnummer`() {
        val soknadSlot = slot<Soknad>()

        every {
            sakService.opprettSak(any(), any())
        } returns saksnummer

        every {
            soknadRepository.findByIdOrNull("123")
        } returns soknad

        every {
            soknadRepository.save(capture(soknadSlot))
        } returns soknad


        opprettSakTask.doTask(Task(type = "", payload = "123", properties = Properties().apply {
            this[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] = "999"
        }))

        assertThat(soknadSlot.captured.saksnummer).isEqualTo(saksnummer)
    }


    @Test
    internal fun `skal ikke oppdatere saksnummer på søknaden dersom sak ikke opprettes`() {
        val soknadSlot = slot<Soknad>()

        every {
            sakService.opprettSak(any(), any())
        } returns null

        every {
            soknadRepository.findByIdOrNull("123")
        } returns soknad

        every {
            soknadRepository.save(capture(soknadSlot))
        } returns soknad


        opprettSakTask.doTask(Task(type = "", payload = "123", properties = Properties().apply {
            this[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] = "999"
        }))

        assertThat(soknadSlot.captured.saksnummer).isNull()
    }

    @Test
    fun `Skal gå til OppdaterBehandleSakOppgaveTask når sak er opprettet`() {
        val slot = slot<Task>()
        every {
            soknadRepository.findByIdOrNull("123")
        } returns soknad

        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        opprettSakTask.onCompletion(Task(type = "", payload = "123", properties = Properties()))

        Assertions.assertEquals(OppdaterBehandleSakOppgaveTask.TYPE, slot.captured.type)
    }

    @Test
    fun `Skal gå til HentSaksnummerFraJoarkTask når sak er ikke er opprettet`() {
        val slot = slot<Task>()
        every {
            soknadRepository.findByIdOrNull("123")
        } returns soknad.copy(saksnummer = null)

        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        opprettSakTask.onCompletion(Task(type = "", payload = "123", properties = Properties()))

        Assertions.assertEquals(HentSaksnummerFraJoarkTask.HENT_SAKSNUMMER_FRA_JOARK, slot.captured.type)
    }

}
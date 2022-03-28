package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.JournalføringHendelseRecordVars.JOURNALPOST_DIGITALSØKNAD
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.DateTimeService
import no.nav.familie.ef.mottak.service.SakService
import no.nav.familie.ef.mottak.task.LagBehandleSakOppgaveTask
import no.nav.familie.ef.mottak.task.LagJournalføringsoppgaveTask
import no.nav.familie.ef.mottak.task.OppdaterBehandleSakOppgaveTask
import no.nav.familie.ef.mottak.task.OpprettSakTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.prosessering.error.RekjørSenereException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Properties

internal class OpprettSakTaskTest {

    private val sakService: SakService = mockk()
    private val søknadRepository: SøknadRepository = mockk()
    private val taskRepository: TaskRepository = mockk()
    private val dateTimeService: DateTimeService = mockk()
    private val opprettSakTask = OpprettSakTask(taskRepository, sakService, dateTimeService, søknadRepository)

    private val saksnummer = "A01"
    private val soknad = søknad(saksnummer = saksnummer, journalpostId = JOURNALPOST_DIGITALSØKNAD)

    private val soknadSlot = slot<Søknad>()

    @BeforeEach
    internal fun setUp() {
        every {
            søknadRepository.findByIdOrNull("123")
        } returns soknad
        every { dateTimeService.now() } returns LocalDateTime.of(2020, 1, 1, 12, 0)
        every {
            søknadRepository.insert(capture(soknadSlot))
        } returns soknad
    }

    @Test
    internal fun `skal opprette sak og persistere saksnummer`() {
        every { taskRepository.save(any()) } answers { firstArg() }
        mockOpprettSak()

        opprettSakTask.doTask(lagTask())

        assertThat(soknadSlot.captured.saksnummer).isEqualTo(saksnummer)
    }

    @Test
    internal fun `skal ikke oppdatere saksnummer på søknaden dersom sak ikke opprettes`() {
        every { taskRepository.save(any()) } answers { firstArg() }
        mockOpprettSak(sakOpprettet = false)

        opprettSakTask.doTask(lagTask())

        assertThat(soknadSlot.captured.saksnummer).isNull()
    }

    @Test
    fun `Skal gå til OppdaterBehandleSakOppgaveTask når sak er opprettet`() {
        val slot = slot<Task>()
        mockOpprettSak()

        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        opprettSakTask.doTask(lagTask())

        assertEquals(OppdaterBehandleSakOppgaveTask.TYPE, slot.captured.type)
    }

    @Test
    fun `Skal gå til HentSaksnummerFraJoarkTask når sak er ikke er opprettet`() {
        val slot = slot<Task>()
        mockOpprettSak(sakOpprettet = false)

        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        opprettSakTask.doTask(lagTask())

        assertEquals(LagJournalføringsoppgaveTask.TYPE, slot.captured.type)
    }

    @Test
    internal fun `skal opprette ny task hvis opprett task feiler og det er natt`() {
        val slot = slot<Task>()
        every { dateTimeService.now() } returns LocalDate.of(2021, 1, 1).atTime(22, 0)
        every { sakService.opprettSak(any(), any()) } throws RuntimeException("test-feil")

        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }


        val throwable = catchThrowable { opprettSakTask.doTask(lagTask()) }
        assertThat(throwable).isInstanceOf(RekjørSenereException::class.java)
        assertThat((throwable as RekjørSenereException).triggerTid)
                .isEqualTo(dateTimeService.now().toLocalDate().plusDays(1).atTime(6, 0))

        verify(exactly = 0) {
            taskRepository.save(any())
        }
    }

    @Test
    internal fun `skal opprette ny task hvis opprett task feiler og det er natt kl 02 - sett ny task til samme dag`() {
        val slot = slot<Task>()
        every { dateTimeService.now() } returns LocalDate.of(2021, 1, 1).atTime(2, 0)
        every { sakService.opprettSak(any(), any()) } throws RuntimeException("Feil")
        every {
            taskRepository.save(capture(slot))
        } answers {
            slot.captured
        }

        val throwable = catchThrowable { opprettSakTask.doTask(lagTask()) }
        assertThat(throwable).isInstanceOf(RekjørSenereException::class.java)
        assertThat((throwable as RekjørSenereException).triggerTid)
                .isEqualTo(dateTimeService.now().toLocalDate().atTime(6, 0))

        verify(exactly = 0) {
            taskRepository.save(any())
        }
    }

    private fun lagTask() = Task(type = OpprettSakTask.TYPE, payload = "123", properties = Properties().apply {
        this[LagBehandleSakOppgaveTask.behandleSakOppgaveIdKey] = "999"
    })

    private fun mockOpprettSak(sakOpprettet: Boolean = true) {
        val response = if (sakOpprettet) saksnummer else null
        every {
            sakService.opprettSak(any(), any())
        } returns response
    }

}
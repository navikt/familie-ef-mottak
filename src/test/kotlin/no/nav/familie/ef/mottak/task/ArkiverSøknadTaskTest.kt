package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.util.Properties
import java.util.UUID

internal class ArkiverSøknadTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val søknadRepository: SøknadRepository = mockk()
    private val arkiverSøknadTaskTest: ArkiverSøknadTask = ArkiverSøknadTask(mockk(), taskRepository, søknadRepository)

    @Test
    fun `Skal gå til LagOppgaveTask når arkiver søknad task er utført`() {

        every { søknadRepository.findByIdOrNull(any()) } returns søknad()
        val slot = slot<List<Task>>()
        every {
            taskRepository.saveAll(capture(slot))
        } answers {
            slot.captured
        }

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = "", properties = Properties()))

        assertEquals(LagBehandleSakOppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette behandle-sak-oppgave-task hvis det er en ny søknad om skolepenger`() {
        val slot = slot<List<Task>>()
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_SKOLEPENGER)

        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { søknadRepository.findByIdOrNull(any()) } returns soknad

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(LagBehandleSakOppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette behandle-sak-oppgave-task hvis det er en ny søknad om barnetilsyn`() {
        val slot = slot<List<Task>>()
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_BARNETILSYN)
        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { søknadRepository.findByIdOrNull(any()) } returns soknad

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(LagBehandleSakOppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette journalføringsoppgave hvis det er et nytt arbeidssøkerskjema`() {
        val slot = slot<List<Task>>()
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER)
        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { søknadRepository.findByIdOrNull(any()) } returns soknad

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(LagJournalføringsoppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal generere ny eventId i metadata ved oppretting av ArkiverSøknadTask`() {
        val slot = slot<List<Task>>()
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_SKOLEPENGER)

        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { søknadRepository.findByIdOrNull(any()) } returns soknad
        val uuid = UUID.randomUUID()
        arkiverSøknadTaskTest.onCompletion(Task(type = "",
                                                payload = soknad.id,
                                                properties = Properties().apply { this["eventId"] = uuid }))

        assertNotEquals(uuid, slot.captured[1].metadata["eventID"])
    }
}
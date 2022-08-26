package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties
import java.util.UUID

internal class ArkiverSøknadTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val arkiverSøknadTaskTest: ArkiverSøknadTask = ArkiverSøknadTask(mockk(), taskRepository)

    private val slot = slot<List<Task>>()

    @BeforeEach
    internal fun setUp() {
        slot.clear()
        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
    }

    @Test
    fun `Skal gå til LagJournalføringsoppgaveTask når arkiver søknad task er utført`() {
        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = "", properties = Properties()))

        assertEquals(VelgAutomatiskEllerManuellFlytTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette LagJournalføringsoppgaveTask hvis det er en ny søknad om skolepenger`() {
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_SKOLEPENGER)

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(VelgAutomatiskEllerManuellFlytTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette LagJournalføringsoppgaveTask hvis det er en ny søknad om barnetilsyn`() {
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_BARNETILSYN)
        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(VelgAutomatiskEllerManuellFlytTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette journalføringsoppgave hvis det er et nytt arbeidssøkerskjema`() {
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER)

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(VelgAutomatiskEllerManuellFlytTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.TYPE, slot.captured[1].type)
    }

    @Test
    fun `Skal generere ny eventId i metadata ved oppretting av ArkiverSøknadTask`() {
        val soknad = søknad(dokumenttype = DOKUMENTTYPE_SKOLEPENGER)
        val uuid = UUID.randomUUID()

        arkiverSøknadTaskTest.onCompletion(
            Task(
                type = "",
                payload = soknad.id,
                properties = Properties().apply { this["eventId"] = uuid }
            )
        )

        assertNotEquals(uuid, slot.captured[1].metadata["eventID"])
    }
}

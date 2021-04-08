package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import java.util.*

internal class ArkiverSøknadTaskTest {

    private val taskRepository: TaskRepository = mockk()
    private val featureToggleService: FeatureToggleService = mockk()
    private val søknadRepository: SoknadRepository = mockk()
    private val arkiverSøknadTaskTest: ArkiverSøknadTask = ArkiverSøknadTask(mockk(), taskRepository, featureToggleService, søknadRepository)

    @Test
    fun `Skal gå til LagOppgaveTask når journalføring er utført`() {
        every { featureToggleService.isEnabled(any()) } returns false
        every { søknadRepository.findByIdOrNull(any()) } returns soknad()
        val slot = slot<List<Task>>()
        every {
            taskRepository.saveAll(capture(slot))
        } answers {
            slot.captured
        }

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = "", properties = Properties()))

        assertEquals(LagJournalføringsoppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette behandle-sak-oppgave-task hvis det er en ny søknad om skolepenger`() {
        val slot = slot<List<Task>>()
        val soknad = Soknad(id = UUID.randomUUID().toString(),
            fnr = "12345678901",
            søknadJson = "",
            dokumenttype = DOKUMENTTYPE_SKOLEPENGER
        )
        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { featureToggleService.isEnabled(any()) } returns true
        every { søknadRepository.findByIdOrNull(any()) } returns soknad

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(LagBehandleSakOppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette behandle-sak-oppgave-task hvis det er en ny søknad om barnetilsyn`() {
        val slot = slot<List<Task>>()
        val soknad = Soknad(id = UUID.randomUUID().toString(),
            fnr = "12345678901",
            søknadJson = "",
            dokumenttype = DOKUMENTTYPE_BARNETILSYN
        )
        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { featureToggleService.isEnabled(any()) } returns true
        every { søknadRepository.findByIdOrNull(any()) } returns soknad

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(LagBehandleSakOppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, slot.captured[1].type)
    }

    @Test
    fun `Skal opprette journalføringsoppgave hvis det er et nytt arbeidssøkerskjema`() {
        val slot = slot<List<Task>>()
        val soknad = Soknad(id = UUID.randomUUID().toString(),
            fnr = "12345678901",
            søknadJson = "",
            dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
        )
        every { taskRepository.saveAll(capture(slot)) } answers { slot.captured }
        every { featureToggleService.isEnabled(any()) } returns true
        every { søknadRepository.findByIdOrNull(any()) } returns soknad

        arkiverSøknadTaskTest.onCompletion(Task(type = "", payload = soknad.id, properties = Properties()))

        assertEquals(LagJournalføringsoppgaveTask.TYPE, slot.captured[0].type)
        assertEquals(SendDokumentasjonsbehovMeldingTilDittNavTask.SEND_MELDING_TIL_DITT_NAV, slot.captured[1].type)
    }

    private fun soknad(taskOpprettet: Boolean = false, opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(12)) =
        Soknad(søknadJson = "",
            fnr = "11111122222",
            dokumenttype = "type",
            opprettetTid = opprettetTid,
            taskOpprettet = taskOpprettet)
}
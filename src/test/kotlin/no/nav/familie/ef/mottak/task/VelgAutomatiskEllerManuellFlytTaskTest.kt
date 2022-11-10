package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties

internal class VelgAutomatiskEllerManuellFlytTaskTest {
    private val taskService: TaskService = mockk()
    private val saksbehandlingClient: SaksbehandlingClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val featureToggleService: FeatureToggleService = mockk()
    private val velgAutomatiskEllerManuellFlytTask =
        VelgAutomatiskEllerManuellFlytTask(taskService, søknadService, saksbehandlingClient, featureToggleService)

    private val arbeidssøkerSkjemaId = "999L"
    private val overgangsstønadSøknadId = "123L"

    @BeforeEach
    internal fun setUp() {
        every {
            søknadService.get(overgangsstønadSøknadId)
        } returns Søknad(
            søknadJson = EncryptedString(""),
            dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
            journalpostId = "1234",
            fnr = FnrGenerator.generer()
        )
        every {
            søknadService.get(arbeidssøkerSkjemaId)
        } returns Søknad(
            søknadJson = EncryptedString(""),
            dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
            journalpostId = "1234",
            fnr = FnrGenerator.generer()
        )
    }

    @Test
    internal fun `skal velge automatisk flyt hvis det kan opprettes førstegangsbehandling`() {
        every { featureToggleService.isEnabled(any()) } returns true
        every { saksbehandlingClient.kanOppretteFørstegangsbehandling(any(), any()) } returns true
        val taskSlot = slot<Task>()
        every { taskService.save(capture(taskSlot)) } answers { taskSlot.captured }

        velgAutomatiskEllerManuellFlytTask.doTask(Task(type = "", payload = overgangsstønadSøknadId, properties = Properties()))
        Assertions.assertThat(taskSlot.captured.type).isEqualTo(automatiskJournalføringFlyt().first().type)
    }

    @Test
    internal fun `skal velge manuell flyt hvis det ikke kan opprettes førstegangsbehandling`() {
        every { featureToggleService.isEnabled(any()) } returns true
        every { saksbehandlingClient.kanOppretteFørstegangsbehandling(any(), any()) } returns false
        val taskSlot = slot<Task>()
        every { taskService.save(capture(taskSlot)) } answers { taskSlot.captured }

        velgAutomatiskEllerManuellFlytTask.doTask(Task(type = "", payload = overgangsstønadSøknadId, properties = Properties()))
        Assertions.assertThat(taskSlot.captured.type).isEqualTo(manuellJournalføringFlyt().first().type)
    }

    @Test
    internal fun `skal velge manuell flyt hvis featuretoggle er slått av`() {
        every { featureToggleService.isEnabled(any()) } returns false
        val taskSlot = slot<Task>()
        every { taskService.save(capture(taskSlot)) } answers { taskSlot.captured }

        velgAutomatiskEllerManuellFlytTask.doTask(Task(type = "", payload = overgangsstønadSøknadId, properties = Properties()))
        Assertions.assertThat(taskSlot.captured.type).isEqualTo(manuellJournalføringFlyt().first().type)
    }

    @Test
    internal fun `skal velge manuell flyt for arbeidssøknadsskjema`() {
        every { featureToggleService.isEnabled(any()) } returns true
        val taskSlot = slot<Task>()
        every { taskService.save(capture(taskSlot)) } answers { taskSlot.captured }

        velgAutomatiskEllerManuellFlytTask.doTask(Task(type = "", payload = arbeidssøkerSkjemaId, properties = Properties()))
        Assertions.assertThat(taskSlot.captured.type).isEqualTo(manuellJournalføringFlyt().first().type)
    }
}

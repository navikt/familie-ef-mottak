package no.nav.familie.ef.mottak.task

import io.mockk.MockKMatcherScope
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.AutomatiskJournalføringService
import no.nav.familie.ef.mottak.service.MappeService
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import no.nav.familie.util.FnrGenerator
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Properties

internal class AutomatiskJournalførTaskTest {

    val automatiskJournalføringService: AutomatiskJournalføringService = mockk()
    val søknadService: SøknadService = mockk()
    val taskService: TaskService = mockk()
    val integrasjonerClient: IntegrasjonerClient = mockk(relaxed = true)
    val mappeService: MappeService = mockk(relaxed = true)

    val automatiskJournalførTask = AutomatiskJournalførTask(
        taskService = taskService,
        automatiskJournalføringService = automatiskJournalføringService,
        søknadService = søknadService,
        integrasjonerClient = integrasjonerClient,
        mappeService = mappeService,
    )

    private val journalpostId = "123"
    private val task: Task
        get() {
            val task =
                Task(type = AutomatiskJournalførTask.TYPE, payload = overgangsstønadSøknadId, properties = Properties())
            task.metadata.apply {
                this["journalpostId"] = journalpostId
            }
            return task
        }

    private val overgangsstønadSøknadId = "123L"
    private val personIdent = FnrGenerator.generer()
    private val mappeId = 1234L

    @BeforeEach
    internal fun setUp() {
        every {
            søknadService.get(overgangsstønadSøknadId)
        } returns Søknad(
            søknadJson = EncryptedString(""),
            dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
            journalpostId = "1234",
            fnr = personIdent,
        )
    }

    @Test
    internal fun `Skal bruke fallback manuell journalføring dersom vi får exception når vi kaller på ef-sak`() {
        val taskSlot = slot<Task>()
        every { taskService.save(capture(taskSlot)) } answers { taskSlot.captured }
        every { automatiskJournalføring() } returns false

        automatiskJournalførTask.doTask(task)

        Assertions.assertThat(taskSlot.captured.type).isEqualTo(manuellJournalføringFlyt().first().type)
    }

    @Test
    internal fun `Skal ikke bruke fallback til manuell journalføring når alt er ok`() {
        every { automatiskJournalføring() } returns true
        automatiskJournalførTask.doTask(task)
        verify(exactly = 0) { taskService.save(any()) }
    }

    @Test
    internal fun `skal finne og sette mappe ved automatisk journalføring`() {
        every { integrasjonerClient.finnBehandlendeEnhetForPersonMedRelasjoner(any()) } returns listOf(Enhet("4489", "NAY"))
        every { mappeService.finnMappeIdForSøknadOgEnhet(any(), any()) } returns mappeId
        every { automatiskJournalføring() } returns true
        automatiskJournalførTask.doTask(task)
        verify { automatiskJournalføringService.journalførAutomatisk(personIdent, journalpostId, any(), mappeId) }
    }

    private fun MockKMatcherScope.automatiskJournalføring() =
        automatiskJournalføringService.journalførAutomatisk(
            any(),
            any(),
            any(),
            any(),
        )
}

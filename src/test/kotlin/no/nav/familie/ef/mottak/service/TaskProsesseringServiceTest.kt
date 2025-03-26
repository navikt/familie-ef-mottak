package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.log.mdc.MDCConstants
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.assertj.core.api.Assertions.assertThat
import org.jboss.logging.MDC
import org.junit.jupiter.api.Test

internal class TaskProsesseringServiceTest {
    private val taskService: TaskService = mockk(relaxed = true)
    private val søknadRepository: SøknadRepository = mockk(relaxed = true)
    private val ettersendingRepository: EttersendingRepository = mockk(relaxed = true)

    private val taskProsesseringService = TaskProsesseringService(taskService, søknadRepository, ettersendingRepository)

    private val ettersending =
        Ettersending(
            ettersendingJson = EncryptedString(data = ""),
            ettersendingPdf = null,
            stønadType = StønadType.BARNETILSYN.toString(),
            journalpostId = null,
            fnr = "",
            taskOpprettet = false,
        )

    @Test
    fun `Skal opprette callId pr stønadstype på ettersendingstasks `() {
        MDC.put(MDCConstants.MDC_CALL_ID, "Heipådeg")
        val taskSlot = slot<Task>()

        every { taskService.save(capture(taskSlot)) } answers { firstArg() }
        every { ettersendingRepository.update(any()) } answers { firstArg() }

        val callIdPre = MDC.get(MDCConstants.MDC_CALL_ID) as? String

        taskProsesseringService.startTaskProsessering(ettersending = ettersending)

        assertThat(taskSlot.captured.callId).isEqualTo(callIdPre + "_" + StønadType.BARNETILSYN.toString())

        taskProsesseringService.startTaskProsessering(ettersending = ettersending.copy(stønadType = StønadType.OVERGANGSSTØNAD.toString()))
        assertThat(taskSlot.captured.callId).isEqualTo(callIdPre + "_" + "OVERGANGSSTONAD")
    }

    @Test
    fun `skal opprette lagPdfKvitteringTask og SendSøknadMottattTilDittNavTask og sette taskOpprettet på søknaden til true`() {
        val soknad = søknad()
        val taskSlot = slot<Task>()
        val soknadSlot = slot<Søknad>()
        every { taskService.save(capture(taskSlot)) }
            .answers { taskSlot.captured }
        every { søknadRepository.update(capture(soknadSlot)) }
            .answers { soknadSlot.captured }

        taskProsesseringService.startTaskProsessering(soknad)

        assertThat(taskSlot.captured.payload).isEqualTo(soknad.id)
        assertThat(soknadSlot.captured.taskOpprettet).isTrue
    }
}

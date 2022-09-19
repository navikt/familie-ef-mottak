package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.service.ArkiveringService
import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

internal class ArkiverEttersendingTaskTest {

    val ettersendingService: EttersendingService = mockk()
    val taskRepository: TaskRepository = mockk()

    val arkiveringService: ArkiveringService = mockk()

    private val arkiverEttersendingTask: ArkiverEttersendingTask =
        ArkiverEttersendingTask(arkiveringService, taskRepository, ettersendingService)

    private val conflictException =
        HttpClientErrorException.Conflict.create(null, HttpStatus.CONFLICT, null, null, null, null)

    private val ettersending = Ettersending(
        id = UUID.randomUUID(),
        ettersendingJson = EncryptedString(data = ""),
        stønadType = "",
        fnr = "",
        taskOpprettet = false,
        opprettetTid = LocalDateTime.now()
    )

    @Test
    fun `skal hente journalpostId fra joark hvis den allerede finnes for eksternReferanseId`() {
        val uuid = UUID.randomUUID().toString()
        val forventetJournalpostId = "123"

        every { arkiveringService.journalførEttersending(any()) } throws conflictException

        every { ettersendingService.hentEttersending(any()) } returns ettersending
        every {
            arkiveringService.hentJournalpostIdForBrukerOgEksternReferanseId(
                any(),
                any()
            )
        } returns lagJournalpost(forventetJournalpostId)

        val task = Task(type = "", payload = uuid, properties = Properties())
        arkiverEttersendingTask.doTask(task)
        Assertions.assertThat(task.metadata["journalpostId"]).isEqualTo(forventetJournalpostId)
    }

    @Test
    fun `Task skal feile hvis det kastes exception vi ikke håndterer`() {
        val uuid = UUID.randomUUID().toString()
        every { arkiveringService.journalførEttersending(any()) } throws IllegalStateException()
        assertThrows<Exception> {
            arkiverEttersendingTask.doTask(
                Task(
                    type = "",
                    payload = uuid,
                    properties = Properties()
                )
            )
        }
    }

    private fun lagJournalpost(forventetJournalpostId: String) = Journalpost(
        journalpostId = forventetJournalpostId,
        journalposttype = Journalposttype.I,
        journalstatus = Journalstatus.MOTTATT,
        dokumenter = listOf(),
        relevanteDatoer = listOf()
    )
}

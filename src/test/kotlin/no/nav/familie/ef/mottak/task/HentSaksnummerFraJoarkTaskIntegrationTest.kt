package no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.service.HentJournalpostService
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.prosessering.internal.TaskStepExecutorService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpClientErrorException
import java.time.LocalDateTime
import java.util.*

@Configuration
@Profile("test-hent-saksnummer-fra-joark")
@Primary
class HentSaksnummerFraJoarkTestConfig {

    @Bean fun hentJournalpostService(): HentJournalpostService = mockk()
}

@ActiveProfiles("local", "test-hent-saksnummer-fra-joark")
internal class HentSaksnummerFraJoarkTaskIntegrationTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var taskRepository: TaskRepository
    @Autowired lateinit var hentJournalpostService: HentJournalpostService
    @Autowired lateinit var taskStepExecutorService: TaskStepExecutorService


    @Test
    internal fun `skal sette triggerTid frem i tid`() {
        val payloadId = UUID.randomUUID().toString()
        val task = taskRepository.save(Task(
                type = HentSaksnummerFraJoarkTask.TYPE,
                payload = payloadId
        ))
        every { hentJournalpostService.hentSaksnummer(any()) } throws
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "", HttpHeaders.EMPTY, byteArrayOf(12), null)

        taskStepExecutorService.pollAndExecute()
        val start = System.currentTimeMillis()
        while (true) {
            try {
                val taskAfterUpdate = taskRepository.findById(task.id!!).get()
                if (taskAfterUpdate.status == Status.KLAR_TIL_PLUKK
                    && taskAfterUpdate.triggerTid?.isAfter(LocalDateTime.now().plusHours(1)) == true) {
                    return
                } else if (System.currentTimeMillis() - start < 1_000) {
                    taskStepExecutorService.pollAndExecute()
                    Thread.sleep(100)
                } else {
                    throw RuntimeException("For lang tid...")
                }
            } catch (e: Exception) {
                if (System.currentTimeMillis() - start > 1_000) {
                    throw RuntimeException("For lang tid...", e)
                } else {
                    Thread.sleep(100)
                }
            }
        }
    }
}
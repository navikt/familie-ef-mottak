package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.TaskMetricRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("local")
internal class TaskMetricServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var taskRepository: TaskRepository
    @Autowired lateinit var taskMetricRepository: TaskMetricRepository
    @Autowired lateinit var soknadRepository: SoknadRepository
    @Autowired lateinit var taskMetricService: TaskMetricService

    @Test
    internal fun `henting av metrics går fint`() {
        taskRepository.deleteAll()
        soknadRepository.save(soknad())
        //oppretter 2 søknader som har opprettet tid nå som ikke skal vises
        soknadRepository.save(soknad(opprettetTid = LocalDateTime.now()))
        soknadRepository.save(soknad(opprettetTid = LocalDateTime.now()))
        //oppretter 2 søknader som har taskOpprettet = true som ikke skal vises
        soknadRepository.save(soknad(true))
        soknadRepository.save(soknad(true))

        taskRepository.save(Task("test2", UUID.randomUUID().toString()).copy(status = Status.FEILET))

        assertThat(soknadRepository.countByTaskOpprettetFalseAndOpprettetTidBefore()).isEqualTo(1)
        assertThat(taskMetricRepository.finnFeiledeTasks()).hasSize(1)

        taskMetricService.oppdaterMetrikker()
    }

    private fun soknad(taskOpprettet: Boolean = false, opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(12)) =
            Soknad(søknadJson = "",
                   fnr = "11111122222",
                   dokumenttype = "type",
                   opprettetTid = opprettetTid,
                   taskOpprettet = taskOpprettet)
}

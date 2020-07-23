package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.DbMigreringRepository
import no.nav.familie.ef.mottak.task.ArkiverSøknadTask
import no.nav.familie.ef.mottak.task.LagJournalføringsoppgaveTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import java.util.*

@ActiveProfiles("local")
internal class DbMigreringServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var dbMigreringRepository: DbMigreringRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    lateinit var dbMigreringService: DbMigreringService

    @BeforeEach
    fun setUp() {
        dbMigreringService = DbMigreringService(dbMigreringRepository)
    }

    @Test
    fun `dbMigrering oppdaterer task med korrekt verdi`() {
        taskRepository.saveAndFlush(Task.nyTask("lagOppgave", "1", Properties()))
        taskRepository.saveAndFlush(Task.nyTask("lagOppgave", "2", Properties()))
        taskRepository.saveAndFlush(Task.nyTask("journalførSøknad", "3", Properties()))
        taskRepository.saveAndFlush(Task.nyTask("journalførSøknad", "4", Properties()))

        val alleFør = taskRepository.findAll()
        assertThat(alleFør.filter { it.taskStepType == "lagOppgave" }.size).isEqualTo(2)
        assertThat(alleFør.filter { it.taskStepType == "journalførSøknad" }.size).isEqualTo(2)

        dbMigreringService.dbMigrering()

        val alleEtter = taskRepository.findAll()
        assertThat(alleEtter.filter { it.taskStepType == "lagOppgave" }).isEmpty()
        assertThat(alleEtter.filter { it.taskStepType == "journalførSøknad" }).isEmpty()
        assertThat(alleEtter.filter { it.taskStepType == LagJournalføringsoppgaveTask.TYPE }.size).isEqualTo(2)
        assertThat(alleEtter.filter { it.taskStepType == ArkiverSøknadTask.TYPE }.size).isEqualTo(2)

    }
}
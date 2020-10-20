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
        taskRepository.save(Task("lagOppgave", "1", Properties()))
        taskRepository.save(Task("lagOppgave", "2", Properties()))
        taskRepository.save(Task("journalførSøknad", "3", Properties()))
        taskRepository.save(Task("journalførSøknad", "4", Properties()))

        val alleFør = taskRepository.findAll()
        assertThat(alleFør.filter { it.type == "lagOppgave" }.size).isEqualTo(2)
        assertThat(alleFør.filter { it.type == "journalførSøknad" }.size).isEqualTo(2)

        dbMigreringService.dbMigrering()

        val alleEtter = taskRepository.findAll()
        assertThat(alleEtter.filter { it.type == "lagOppgave" }).isEmpty()
        assertThat(alleEtter.filter { it.type == "journalførSøknad" }).isEmpty()
        assertThat(alleEtter.filter { it.type == LagJournalføringsoppgaveTask.TYPE }.size).isEqualTo(2)
        assertThat(alleEtter.filter { it.type == ArkiverSøknadTask.TYPE }.size).isEqualTo(2)

    }
}
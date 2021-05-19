package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.TaskRepositoryUtvidet
import no.nav.familie.ef.mottak.task.LagEksternJournalføringsoppgaveTask
import no.nav.familie.ef.mottak.task.SjekkOmJournalpostHarFåttEnSak
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class TaskRepositoryUtvidetTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var taskRepositoryUtvidet: TaskRepositoryUtvidet

    @Test
    internal fun name() {
        taskRepositoryUtvidet.save(Task(type = LagEksternJournalføringsoppgaveTask.TYPE,
                                        payload = "tullball").copy(status = Status.MANUELL_OPPFØLGING))
    }

    @Test
    internal fun `skal ikke få treff dersom det ikke eksisterer en task med riktig journalpostId som payload`() {
        val journalpostId = "12345678"
        taskRepositoryUtvidet.save(Task(
                type = LagEksternJournalføringsoppgaveTask.TYPE,
                payload = "tullball"
        ))

        assertThat(taskRepositoryUtvidet.existsByPayloadAndType(journalpostId, LagEksternJournalføringsoppgaveTask.TYPE)).isFalse
    }

    @Test
    internal fun `skal ikke få treff dersom det ikke eksisterer en task med riktig type`() {
        val journalpostId = "12345678"
        taskRepositoryUtvidet.save(Task(
                type = SjekkOmJournalpostHarFåttEnSak.TYPE,
                payload = journalpostId
        ))

        assertThat(taskRepositoryUtvidet.existsByPayloadAndType(journalpostId, LagEksternJournalføringsoppgaveTask.TYPE)).isFalse
    }

    @Test
    internal fun `skal få treff dersom det  eksisterer en task med riktig type og journalpostId som payload`() {
        val journalpostId = "12345678"
        taskRepositoryUtvidet.save(Task(
                type = LagEksternJournalføringsoppgaveTask.TYPE,
                payload = journalpostId
        ))

        assertThat(taskRepositoryUtvidet.existsByPayloadAndType(journalpostId, LagEksternJournalføringsoppgaveTask.TYPE)).isTrue
    }


}

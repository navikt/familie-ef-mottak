package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.FeiletTaskMetric
import no.nav.familie.prosessering.domene.Task
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TaskMetricRepository : org.springframework.data.repository.Repository<Task, String> {

    @Query("""SELECT new no.nav.familie.ef.mottak.repository.domain.FeiletTaskMetric(t.taskStepType, count(t.id)) FROM Task t
                    WHERE t.status = 'FEILET'
                    GROUP by t.taskStepType""")
    fun finnFeiledeTasks(): List<FeiletTaskMetric>

}

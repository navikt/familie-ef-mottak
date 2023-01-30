package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.FeiletTaskMetric
import no.nav.familie.prosessering.domene.Task
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository

@Repository
interface TaskMetricRepository : org.springframework.data.repository.Repository<Task, String> {

    @Query(
        """SELECT t.type, count(t.id) as count FROM Task t
                    WHERE t.status = 'FEILET'
                    GROUP by t.type""",
    )
    fun finnFeiledeTasks(): List<FeiletTaskMetric>
}

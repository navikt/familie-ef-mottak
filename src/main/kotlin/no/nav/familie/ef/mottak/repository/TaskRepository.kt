package no.nav.familie.ef.mottak.repository

import no.nav.familie.prosessering.domene.Task
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : CrudRepository<Task, Long> {

    @Query("""SELECT t FROM Task t  
                     WHERE status IN ('KLAR_TIL_PLUKK', 'UBEHANDLET') 
                     ORDER BY opprettetTidspunkt """)
    fun finnAlleHenvendelserKlareForProsessering(): List<Task>

    @Query("select t from Task t where t.payloadId = s.id")
    fun find()


}

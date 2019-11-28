package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SoknadRepository : JpaRepository<Soknad, Long> {

    @Query("SELECT s FROM Soknad s WHERE NOT exists (SELECT t FROM Task t WHERE CAST(s.id as text)  = t.payloadId)")
    fun finnAlleSøknaderUtenTask(): List<Soknad>
}

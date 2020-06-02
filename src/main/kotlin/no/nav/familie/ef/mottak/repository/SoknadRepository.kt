package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SoknadRepository : JpaRepository<Soknad, String> {

    @Query("SELECT s FROM Soknad s where s.taskOpprettet = false ")
    fun finnAlleSøknaderUtenTask(): List<Soknad>
}

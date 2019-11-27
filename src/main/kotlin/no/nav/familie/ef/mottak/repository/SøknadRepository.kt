package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.prosessering.domene.Task
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SøknadRepository: PagingAndSortingRepository<Soknad,  Long> {

    @Query(nativeQuery = true,
           value = "select * from Soknad s where not exists (select 1 from Task t where s.id = t.payload::bigint )")
    fun finnAlleSøknaderUtenTask(): List<Soknad>

}

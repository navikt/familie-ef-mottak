package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface SøknadRepository: PagingAndSortingRepository<Søknad,  Long> {

    @Query("select s from soknad s where not exists (select t from task t where payloadId = s.id)")
    fun finnAlleSøknaderUtenTask(pageable: Pageable): List<Søknad>
}

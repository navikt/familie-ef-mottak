package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface VedleggRepository : JpaRepository<Vedlegg, String> {

    fun findBySøknadId(søknadId: String): List<Vedlegg>

    @Query(nativeQuery= true, value="SELECT tittel FROM vedlegg WHERE soknad_id=:søknadId")
    fun findTitlerBySøknadId(søknadId: String): List<String>

}
package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository

@Repository
interface VedleggRepository :
    RepositoryInterface<Vedlegg, String>,
    InsertUpdateRepository<Vedlegg> {
    fun findBySøknadId(søknadId: String): List<Vedlegg>

    @Query("SELECT tittel FROM vedlegg WHERE soknad_id=:søknadId")
    fun finnTitlerForSøknadId(søknadId: String): List<String>

    @Modifying
    @Query("DELETE FROM vedlegg WHERE soknad_id = :søknadId")
    fun deleteBySøknadId(søknadId: String)
}

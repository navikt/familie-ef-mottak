package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface EttersendingVedleggRepository : JpaRepository<EttersendingVedlegg, String> {

    fun findByEttersendingId(ettersendingId: String): List<EttersendingVedlegg>

    @Query(nativeQuery = true, value = "SELECT tittel FROM vedlegg WHERE ettersending_id=:ettersendingId")
    fun findTitlerByEtterseningId(ettersendingId: String): List<String>

}
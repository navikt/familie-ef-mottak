package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EttersendingVedleggRepository : JpaRepository<EttersendingVedlegg, String> {

    fun findByEttersendingId(ettersendingId: String): List<EttersendingVedlegg>
}
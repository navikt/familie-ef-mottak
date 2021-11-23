package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EttersendingVedleggRepository : JpaRepository<EttersendingVedlegg, UUID> {

    fun findByEttersendingId(ettersendingId: UUID): List<EttersendingVedlegg>
}
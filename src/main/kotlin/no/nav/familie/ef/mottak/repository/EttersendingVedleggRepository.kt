package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EttersendingVedleggRepository :
    RepositoryInterface<EttersendingVedlegg, UUID>,
    InsertUpdateRepository<EttersendingVedlegg> {

    fun findByEttersendingId(ettersendingId: UUID): List<EttersendingVedlegg>

    @Modifying
    @Query("DELETE FROM ettersending_vedlegg WHERE ettersending_id = :ettersendingId")
    fun deleteAllByEttersendingId(ettersendingId: UUID)
}

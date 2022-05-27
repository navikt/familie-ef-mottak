package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EttersendingVedleggRepository :
    RepositoryInterface<EttersendingVedlegg, UUID>,
    InsertUpdateRepository<EttersendingVedlegg> {

    fun findByEttersendingId(ettersendingId: UUID): List<EttersendingVedlegg>
}

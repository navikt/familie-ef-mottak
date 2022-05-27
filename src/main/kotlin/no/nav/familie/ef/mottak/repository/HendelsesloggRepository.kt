package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HendelsesloggRepository :
    RepositoryInterface<Hendelseslogg, UUID>,
    InsertUpdateRepository<Hendelseslogg> {

    fun existsByHendelseId(hendelseId: String): Boolean
}

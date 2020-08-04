package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Hendelseslogg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HendelsesloggRepository : JpaRepository<Hendelseslogg, UUID> {

    fun existsByHendelseId(hendelseId: String): Boolean
}
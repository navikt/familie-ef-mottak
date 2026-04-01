package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

interface EttersendingRepository : CrudRepository<Ettersending, UUID> {
    fun findFirstByTaskOpprettetIsFalse(): Ettersending?

    fun findByJournalpostId(journalpostId: String): Ettersending?

    fun findAllByFnr(personIdent: String): List<Ettersending>

    @Query("""SELECT id FROM ettersending WHERE journalpost_id IS NOT NULL AND opprettet_tid < :tidspunkt""")
    fun finnEttersendingerKlarTilSletting(tidspunkt: LocalDateTime): List<UUID>
}

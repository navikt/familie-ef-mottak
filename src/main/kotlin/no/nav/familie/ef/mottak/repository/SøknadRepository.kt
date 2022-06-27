package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SøknadRepository :
    RepositoryInterface<Søknad, String>,
    InsertUpdateRepository<Søknad> {

    fun findFirstByTaskOpprettetIsFalse(): Søknad?

    fun findByJournalpostId(jounalpostId: String): Søknad?

    @Query(
        """
        WITH q AS (SELECT s.*, ROW_NUMBER() OVER (PARTITION BY dokumenttype ORDER BY opprettet_tid DESC) rn 
                   FROM soknad s WHERE fnr=:fnr)
        SELECT * FROM q WHERE rn = 1
    """
    )
    fun finnSisteSøknadenPerStønadtype(fnr: String): List<Søknad>

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

    @Query("""SELECT id FROM soknad WHERE journalpost_id IS NOT NULL AND soknad_pdf IS NOT NULL AND opprettet_tid < :tidspunkt""")
    fun finnSøknaderKlarTilReduksjon(tidspunkt: LocalDateTime): List<String>

    @Query("""SELECT id FROM soknad WHERE journalpost_id IS NOT NULL AND soknad_pdf IS NULL AND opprettet_tid < :tidspunkt""")
    fun finnSøknaderKlarTilSletting(tidspunkt: LocalDateTime): List<String>
}

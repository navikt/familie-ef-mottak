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
        SELECT * FROM (
        SELECT s.*, ROW_NUMBER() OVER (PARTITION BY dokumenttype ORDER BY opprettet_tid DESC) rn FROM soknad s WHERE fnr=:fnr) q
        WHERE rn = 1
    """,
    )
    fun finnSisteSøknadenPerStønadtype(fnr: String): List<Søknad>

    @Query(
        """
        SELECT * FROM soknad ORDER BY opprettet_tid DESC LIMIT 1
        """,
    )
    fun finnSisteLagredeSøknad(): Søknad

    @Query(
        """
        SELECT * FROM soknad WHERE fnr=:fnr AND dokumenttype=:stønadstype ORDER BY opprettet_tid DESC LIMIT 1
        """,
    )
    fun finnSisteSøknadForPersonOgStønadstype(fnr: String, stønadstype: String): Søknad?

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

    fun findAllByFnr(personIdent: String): List<Søknad>

    @Query("""SELECT id FROM soknad WHERE journalpost_id IS NOT NULL AND soknad_pdf IS NOT NULL AND opprettet_tid < :tidspunkt""")
    fun finnSøknaderKlarTilReduksjon(tidspunkt: LocalDateTime): List<String>

    @Query("""SELECT id FROM soknad WHERE journalpost_id IS NOT NULL AND soknad_pdf IS NULL AND opprettet_tid < :tidspunkt""")
    fun finnSøknaderKlarTilSletting(tidspunkt: LocalDateTime): List<String>
}

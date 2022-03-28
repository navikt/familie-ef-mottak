package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SøknadRepository : RepositoryInterface<Søknad, String>,
                             InsertUpdateRepository<Søknad> {

    fun findFirstByTaskOpprettetIsFalse(): Søknad?

    fun findByJournalpostId(jounalpostId: String): Søknad?

    fun findAllByFnr(fnr: String): List<Søknad>

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

}

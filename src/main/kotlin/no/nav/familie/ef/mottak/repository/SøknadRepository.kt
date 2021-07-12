package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SøknadRepository : JpaRepository<Søknad, String> {

    fun findFirstByTaskOpprettetIsFalse(): Søknad?

    fun findByJournalpostId(jounalpostId: String): Søknad?

    fun findAllByFnr(fnr: String): List<Søknad>

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

}

package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Ettersending
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EttersendingRepository : JpaRepository<Ettersending, String>{
    fun findFirstByTaskOpprettetIsFalse(): Ettersending?

    fun findByJournalpostId(journalpostId: String): Ettersending?

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

}
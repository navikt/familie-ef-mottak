package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.EttersendingDb
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EttersendingRepository : JpaRepository<EttersendingDb, String>{
    fun findFirstByTaskOpprettetIsFalse(): EttersendingDb?

    fun findByJournalpostId(journalpostId: String): EttersendingDb?

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

}
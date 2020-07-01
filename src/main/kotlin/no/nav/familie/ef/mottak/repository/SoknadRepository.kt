package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SoknadRepository : JpaRepository<Soknad, String> {

    fun findFirstByTaskOpprettetIsFalse(): Soknad?

    fun findFirstByVedleggIsNotNull(): Soknad?

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

}

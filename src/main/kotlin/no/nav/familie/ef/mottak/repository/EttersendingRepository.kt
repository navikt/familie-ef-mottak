package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface EttersendingRepository :
    RepositoryInterface<Ettersending, UUID>,
    InsertUpdateRepository<Ettersending> {

    fun findFirstByTaskOpprettetIsFalse(): Ettersending?

    fun findByJournalpostId(journalpostId: String): Ettersending?

    fun countByTaskOpprettetFalseAndOpprettetTidBefore(opprettetTid: LocalDateTime = LocalDateTime.now().minusHours(2)): Long

    fun findAllByFnr(personIdent: String): List<Ettersending>
}

package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SoknadRepository : JpaRepository<Soknad, String> {

    fun findFirstByTaskOpprettetIsFalse(): Soknad?

    fun findFirstByVedleggIsNotNull(): Soknad?
}

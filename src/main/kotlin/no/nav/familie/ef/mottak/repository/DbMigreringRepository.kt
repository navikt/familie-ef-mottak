package no.nav.familie.ef.mottak.repository

import no.nav.familie.prosessering.domene.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface DbMigreringRepository : JpaRepository<Task, Long> {

    @Modifying
    @Query("update Task t set t.type = :newType where t.type = :oldType")
    @Transactional
    fun updateTaskSetTypeForOldTypeUserSetStatusForName(newType: String,
                                                        oldType: String): Int
}

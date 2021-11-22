package no.nav.familie.ef.mottak.repository

import no.nav.familie.prosessering.domene.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface DbMigreringRepository : JpaRepository<Task, Long>

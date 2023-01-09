package no.nav.familie.ef.mottak.repository

import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Repository

@Repository
interface TaskRepositoryUtvidet : org.springframework.data.repository.Repository<Task, Long> {

    fun existsByPayloadAndType(payload: String, type: String): Boolean
}

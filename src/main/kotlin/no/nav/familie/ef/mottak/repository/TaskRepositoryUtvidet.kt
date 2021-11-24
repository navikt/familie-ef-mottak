package no.nav.familie.ef.mottak.repository

import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepositoryUtvidet : TaskRepository {

    fun existsByPayloadAndType(payload: String, type: String): Boolean
}
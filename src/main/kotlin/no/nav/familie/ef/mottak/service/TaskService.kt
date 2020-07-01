package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.task.LagPdfTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class TaskService(private val taskRepository: TaskRepository,
                  private val soknadRepository: SoknadRepository) {

    @Transactional
    fun opprettTaskForSoknad(it: Soknad) {
        taskRepository.save(Task.nyTask(LagPdfTask.LAG_PDF,
                                        it.id,
                                        Properties().apply { this["søkersFødselsnummer"] = it.fnr }))

        soknadRepository.save(it.copy(taskOpprettet = true))
    }

}


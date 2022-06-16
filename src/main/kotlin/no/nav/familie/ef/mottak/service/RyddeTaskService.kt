package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.task.EttersendingsslettingTask
import no.nav.familie.ef.mottak.task.SøknadsreduksjonTask
import no.nav.familie.ef.mottak.task.SøknadsslettingTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class RyddeTaskService(private val taskService: TaskService) {

    fun opprettSøknadsreduksjonTask(søknadId: String) {
        taskService.save(Task(SøknadsreduksjonTask.TYPE, søknadId))
    }

    fun opprettSøknadsslettingTask(søknadId: String) {
        taskService.save(Task(SøknadsslettingTask.TYPE, søknadId))
    }

    fun opprettEttersendingsslettingTask(ettersendingId: UUID) {
        taskService.save(Task(EttersendingsslettingTask.TYPE, ettersendingId.toString()))
    }
}

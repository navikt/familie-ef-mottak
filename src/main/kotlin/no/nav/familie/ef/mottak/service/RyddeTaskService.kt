package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.task.EttersendingsslettingTask
import no.nav.familie.ef.mottak.task.SøknadsreduksjonTask
import no.nav.familie.ef.mottak.task.SøknadsslettingTask
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Properties
import java.util.UUID

@Service
@Transactional
class RyddeTaskService(private val taskService: TaskService) {
    fun opprettSøknadsreduksjonTask(søknadId: String) {
        val properties =
            Properties().apply {
                setProperty("søknadId", søknadId)
            }
        taskService.save(Task(SøknadsreduksjonTask.TYPE, søknadId, properties))
    }

    fun opprettSøknadsslettingTask(søknadId: String) {
        val properties =
            Properties().apply {
                setProperty("søknadId", søknadId)
            }
        taskService.save(Task(SøknadsslettingTask.TYPE, søknadId, properties))
    }

    fun opprettEttersendingsslettingTask(ettersendingId: UUID) {
        val properties =
            Properties().apply {
                setProperty("ettersendingId", ettersendingId.toString())
            }
        taskService.save(Task(EttersendingsslettingTask.TYPE, ettersendingId.toString(), properties))
    }
}

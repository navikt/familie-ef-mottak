package no.nav.familie.ef.mottak.service

import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.repository.TaskMetricRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class TaskMetricService(private val taskMetricRepository: TaskMetricRepository) {

    @Scheduled(initialDelay = 5_000, fixedDelay = 10*60_000)
    fun updateMetrics() {
        taskMetricRepository.finnFeiledeTasks().forEach {
            println("tasks_${it.taskStepType}_feilet ${it.count}")
            Metrics.gauge("tasks_${it.taskStepType}_feilet", it.count)
        }
    }
}

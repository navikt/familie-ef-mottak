package no.nav.familie.ef.mottak.service

import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskMetricRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class TaskMetricService(private val taskMetricRepository: TaskMetricRepository,
                        private val søknadRepository: SøknadRepository) {

    @Scheduled(initialDelay = 5_000, fixedDelay = 5 * 60_000)
    fun oppdaterMetrikker() {
        taskMetricRepository.finnFeiledeTasks().forEach {
            Metrics.gauge("tasks_${it.type}_feilet", it.count)
        }
        Metrics.gauge("soknad_task_ikke_opprettet", søknadRepository.countByTaskOpprettetFalseAndOpprettetTidBefore())
    }
}

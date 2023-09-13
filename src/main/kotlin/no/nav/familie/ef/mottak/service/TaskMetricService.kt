package no.nav.familie.ef.mottak.service

import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskMetricRepository
import no.nav.familie.leader.LeaderClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class TaskMetricService(
    private val taskMetricRepository: TaskMetricRepository,
    private val søknadRepository: SøknadRepository,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 5_000, fixedDelay = 5 * 60_000)
    fun oppdaterMetrikker() {
        when (LeaderClient.isLeader()) {
            true -> oppdaterMetrikkerForOpprettingAvTask()
            false -> logger.info("Er ikke leder - oppdaterer ikke metrikker")
            null -> logger.error("Klarer ikke finne leder? Metrikker oppdateres ikke.")
        }
    }

    private fun oppdaterMetrikkerForOpprettingAvTask() {
        taskMetricRepository.finnFeiledeTasks().forEach {
            Metrics.gauge("tasks_${it.type}_feilet", it.count)
        }
        Metrics.gauge("soknad_task_ikke_opprettet", søknadRepository.countByTaskOpprettetFalseAndOpprettetTidBefore())
    }
}

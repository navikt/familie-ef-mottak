package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.task.LagPdfTask.Companion.LAG_PDF
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class ScheduledEventService(private val taskRepository: TaskRepository,
                            private val soknadRepository: SoknadRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForSøknad() {
        val soknad = soknadRepository.findFirstByTaskOpprettetIsFalse()
        try {
            soknad?.let {
                opprettTask(it)
                logger.info("Task opprettet for søknad med id ${it.id}")
            }
        } catch (e: DataIntegrityViolationException) {
            logger.info("ConstraintViolation ved forsøk på å opprette task for søknad med id ${soknad?.id}")
        }
    }

    @Transactional
    fun opprettTask(soknad: Soknad) {
        taskRepository.save(Task.nyTask(LAG_PDF, soknad.id, Properties().apply { this["søkersFødselsnummer"] = soknad.fnr }))
        soknadRepository.save(soknad.copy(taskOpprettet = true))
    }
}

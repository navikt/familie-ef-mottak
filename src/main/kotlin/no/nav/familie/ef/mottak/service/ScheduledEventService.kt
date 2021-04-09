package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SøknadRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledEventService(private val søknadRepository: SøknadRepository,
                            private val taskProsesseringService: TaskProsesseringService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForSøknad() {
        val soknad = søknadRepository.findFirstByTaskOpprettetIsFalse()
        try {
            soknad?.let {
                taskProsesseringService.startTaskProsessering(it)
                logger.info("Task opprettet for søknad med id ${it.id}")
            }
        } catch (e: DataIntegrityViolationException) {
            logger.info("ConstraintViolation ved forsøk på å opprette task for søknad med id ${soknad?.id}")
        }
    }

}

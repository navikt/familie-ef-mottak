package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SoknadRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledEventService(private val soknadRepository: SoknadRepository,
                            private val taskService: TaskService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForSøknad() {
        val soknad = soknadRepository.findFirstByTaskOpprettetIsFalse()
        try {
            soknad?.let {
                taskService.opprettPdfTaskForSoknad(it)
                taskService.sendSøknadMottattTilDittNavTask(it)
                logger.info("Task opprettet for søknad med id ${it.id}")
            }
        } catch (e: DataIntegrityViolationException) {
            logger.info("ConstraintViolation ved forsøk på å opprette task for søknad med id ${soknad?.id}")
        }
    }

}

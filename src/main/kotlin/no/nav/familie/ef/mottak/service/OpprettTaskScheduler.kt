package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.leader.LeaderClient
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class OpprettTaskScheduler(
    private val søknadRepository: SøknadRepository,
    private val ettersendingRepository: EttersendingRepository,
    private val taskProsesseringService: TaskProsesseringService,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 1000 * 60 * 15)
    fun opprettTaskForSøknad() {
        when (LeaderClient.isLeader()) {
            true -> {
                opprettManglendeTaskForSøknad()
                opprettManglendeTasksForEttersending()
            }
            false -> logger.info("Er ikke leder - leter ikke etter manglende tasks for søknad ")
            null -> logger.warn("Leader election returnerer null. Leter ikke etter manglende tasks")
        }
    }

    private fun opprettManglendeTaskForSøknad() {
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

    private fun opprettManglendeTasksForEttersending() {
        val ettersending = ettersendingRepository.findFirstByTaskOpprettetIsFalse()
        try {
            ettersending?.let {
                taskProsesseringService.startTaskProsessering(it)
                logger.info("Task opprettet for ettersending med id ${it.id}")
            }
        } catch (e: DataIntegrityViolationException) {
            logger.info("ConstraintViolation ved forsøk på å opprette task for ettersending med id ${ettersending?.id}")
        }
    }
}
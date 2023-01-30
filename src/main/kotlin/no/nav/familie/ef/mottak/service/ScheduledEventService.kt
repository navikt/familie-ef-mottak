package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ScheduledEventService(
    private val søknadRepository: SøknadRepository,
    private val ettersendingRepository: EttersendingRepository,
    private val taskProsesseringService: TaskProsesseringService,
    private val ryddeTaskService: RyddeTaskService,
    @Value("\${prosessering.enabled:true}")
    private val prosesserongEnabled: Boolean,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForSøknad() {
        if (!prosesserongEnabled) return

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

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForEttersending() {
        if (!prosesserongEnabled) return

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

    @Scheduled(cron = "\${DB_RYDDING_CRON_EXPRESSION}")
    fun ryddGamleForekomster() {
        val tidspunktFor3MånederSiden = LocalDateTime.now().minusMonths(3)
        val søknaderTilReduksjon = søknadRepository.finnSøknaderKlarTilReduksjon(tidspunktFor3MånederSiden)
        søknaderTilReduksjon.forEach {
            try {
                ryddeTaskService.opprettSøknadsreduksjonTask(it)
                logger.info("Task opprettet for reduksjon av søknad med id $it")
            } catch (e: DataIntegrityViolationException) {
                logger.info("ConstraintViolation ved forsøk på å opprette task for søknadsreduksjon med id $it")
            }
        }
        logger.info("Opprettet ${søknaderTilReduksjon.size} tasker for reduksjon av søknader.")

        val ettersendingerTilSletting = ettersendingRepository.finnEttersendingerKlarTilSletting(tidspunktFor3MånederSiden)
        ettersendingerTilSletting.forEach {
            try {
                ryddeTaskService.opprettEttersendingsslettingTask(it)
                logger.info("Task opprettet for sletting av ettersending med id $it")
            } catch (e: DataIntegrityViolationException) {
                logger.info("ConstraintViolation ved forsøk på å opprette task for søknadssletting med id $it")
            }
        }
        logger.info("Opprettet ${ettersendingerTilSletting.size} tasker for sletting av ettersendinger.")

        // val tidspunktFor6MånederSiden = LocalDateTime.now().minusMonths(6)
        // val søknaderTilSletting = søknadRepository.finnSøknaderKlarTilSletting(tidspunktFor6MånederSiden)
        // TODO Kommenter inn linjer over og slett linje under. Hvis sletting av søknader blir aktuelt.
        val søknaderTilSletting = emptyList<String>()
        søknaderTilSletting.forEach {
            try {
                ryddeTaskService.opprettSøknadsslettingTask(it)
                logger.info("Task opprettet for sletting av søknad med id $it")
            } catch (e: DataIntegrityViolationException) {
                logger.info("ConstraintViolation ved forsøk på å opprette task for ettersendingssletting med id $it")
            }
        }
        logger.info("Opprettet ${søknaderTilSletting.size} tasker for sletting av søknader.")
    }
}

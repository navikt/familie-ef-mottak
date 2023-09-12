package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.leader.LeaderClient
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class FjernProsessertDataScheduledEventService(
    private val søknadRepository: SøknadRepository,
    private val ettersendingRepository: EttersendingRepository,
    private val ryddeTaskService: RyddeTaskService,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(cron = "\${DB_RYDDING_CRON_EXPRESSION}")
    fun ryddGamleForekomster() {
        when (LeaderClient.isLeader()) {
            true -> reduserSøknadsinnholdOgSlettArkiverteEttersendinger()
            false -> logger.info("Er ikke leder - starter ikke ryddejobb")
            null -> logger.error("Klarer ikke finne leder? Starter ikke ryddejobb.")
        }
    }

    private fun reduserSøknadsinnholdOgSlettArkiverteEttersendinger() {
        logger.info("Starter opprydding i database")
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

        val ettersendingerTilSletting =
            ettersendingRepository.finnEttersendingerKlarTilSletting(tidspunktFor3MånederSiden)
        ettersendingerTilSletting.forEach {
            try {
                ryddeTaskService.opprettEttersendingsslettingTask(it)
                logger.info("Task opprettet for sletting av ettersending med id $it")
            } catch (e: DataIntegrityViolationException) {
                logger.info("ConstraintViolation ved forsøk på å opprette task for søknadssletting med id $it")
            }
        }
        logger.info("Opprettet ${ettersendingerTilSletting.size} tasker for sletting av ettersendinger.")
    }
}

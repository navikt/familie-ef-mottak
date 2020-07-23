package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.repository.DbMigreringRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.task.ArkiverSøknadTask
import no.nav.familie.ef.mottak.task.LagJournalføringsoppgaveTask
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DbMigreringService(private val dbMigreringRepository: DbMigreringRepository)  {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 120000, fixedDelay = ÅR)
    @Transactional
    fun dbMigrering() {
        dbMigreringRepository.updateTaskSetTypeForOldTypeUserSetStatusForName(ArkiverSøknadTask.TYPE, "journalførSøknad")
        dbMigreringRepository.updateTaskSetTypeForOldTypeUserSetStatusForName(LagJournalføringsoppgaveTask.TYPE, "lagOppgave")
        logger.info("Migrering av tasktyper fullført.")
    }

    companion object {
        const val ÅR = 1000 * 60 * 60 * 24 * 365L
    }
}

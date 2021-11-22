package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.DbMigreringRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DbMigreringService(@Suppress("unused")
                         private val dbMigreringRepository: DbMigreringRepository) {

    @Suppress("unused")
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 120000, fixedDelay = ÅR)
    @Transactional
    fun dbMigrering() {
        //logger.info("Migrering fullført.")
    }

    companion object {

        const val ÅR = 1000 * 60 * 60 * 24 * 365L
    }
}

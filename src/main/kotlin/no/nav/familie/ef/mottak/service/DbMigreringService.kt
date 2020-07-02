package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.getDataOrThrow
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class DbMigreringService(private val soknadRepository: SoknadRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    @Transactional
    fun dbMigrering() {
        val soknad = soknadRepository.findFirstBySaksnummerStartingWith("{")
        soknad?.let {
            migrerForekomst(it)
            logger.info("Migrert saksnummer for søknad med id ${it.id}")
        }
    }

    private fun migrerForekomst(soknad: Soknad) {
        val jsonMap = objectMapper.readValue<Ressurs<Map<*, *>>>(soknad.saksnummer!!)
        val saksnummer = jsonMap.getDataOrThrow()["saksnummer"].toString()
        soknadRepository.save(soknad.copy(saksnummer = saksnummer))
    }
}

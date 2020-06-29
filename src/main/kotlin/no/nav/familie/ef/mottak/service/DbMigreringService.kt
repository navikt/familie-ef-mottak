package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.transaction.Transactional
import no.nav.familie.ef.mottak.repository.domain.Vedlegg as DbVedlegg

@Service
class DbMigreringService(private val soknadRepository: SoknadRepository,
                         private val vedleggRepository: VedleggRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    @Transactional
    fun dbMigrering() {
        val soknad = soknadRepository.findFirstByVedleggIsNotNull()
        soknad?.let {
            migrerForekomst(it)
            logger.info("Migreert vedlegg for søknad med id ${it.id}")
        }
    }

    fun migrerForekomst(soknad: Soknad) {
        val dbVedlegg = mapVedlegg(soknad)
        vedleggRepository.saveAll(dbVedlegg)
        soknadRepository.save(soknad.copy(vedlegg = null))
    }

    private fun mapVedlegg(soknad: Soknad): List<DbVedlegg> {
        if (soknad.vedlegg == null) return emptyList()
        return objectMapper.readValue<List<Vedlegg>>(soknad.vedlegg).map {
            DbVedlegg(søknadId = soknad.id,
                      navn = it.navn,
                      tittel = it.tittel,
                      innhold = Fil(it.bytes))
        }
    }
}

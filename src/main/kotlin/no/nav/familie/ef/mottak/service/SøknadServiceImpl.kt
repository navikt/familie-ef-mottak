package no.nav.familie.ef.mottak.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SøknadServiceImpl(private val registry: MeterRegistry,
                        private val søknadRepository: SøknadRepository,
                        private val søknadClient: SøknadClient) : SøknadService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun motta(søknadDto: SøknadDto): Kvittering {
        val søknad = SøknadMapper.fromDto(søknadDto)
        søknadRepository.save(søknad)
        return Kvittering("Søknad lagre med id ${søknad.id} er registrert mottatt.")
    }

    override fun get(id: Long): Søknad {
        return søknadRepository.findByIdOrNull(id) ?: error("Ugyldig primærnøkkel")
    }

    override fun sendTilSak(søknadId: String) {

        val søknad: Søknad = søknadRepository.findByIdOrNull(søknadId.toLong()) ?: error("")
        val sendTilSakDto = SøknadMapper.toDto(søknad)
        søknadClient.sendTilSak(sendTilSakDto)
    }

    override fun lagreSøknad(søknad: Søknad) {
        søknadRepository.save(søknad)
    }
}

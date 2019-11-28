package no.nav.familie.ef.mottak.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.api.dto.SøknadDto
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SøknadServiceImpl(private val registry: MeterRegistry,
                        private val soknadRepository: SoknadRepository,
                        private val søknadClient: SøknadClient) : SøknadService {

    @Transactional
    override fun motta(søknadDto: SøknadDto): Kvittering {
        val søknad = SøknadMapper.fromDto(søknadDto)
        soknadRepository.save(søknad)
        return Kvittering("Søknad lagre med id ${søknad.id} er registrert mottatt.")
    }

    override fun get(id: Long): Soknad {
        return soknadRepository.findByIdOrNull(id) ?: error("Ugyldig primærnøkkel")
    }

    override fun sendTilSak(søknadId: String) {

        val soknad: Soknad = soknadRepository.findByIdOrNull(søknadId.toLong()) ?: error("")
        val sendTilSakDto = SøknadMapper.fromDto(soknad)
        søknadClient.sendTilSak(sendTilSakDto)
    }

    override fun lagreSøknad(soknad: Soknad) {
        soknadRepository.save(soknad)
    }
}

package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.Søknad
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SøknadServiceImpl(private val soknadRepository: SoknadRepository,
                        private val søknadClient: SøknadClient) : SøknadService {

    @Transactional
    override fun motta(søknad: Søknad): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad)
        soknadRepository.save(søknadDb)
        return Kvittering("Søknad lagret med id ${søknadDb.id} er registrert mottatt.")
    }

    override fun get(id: String): Soknad {
        return soknadRepository.findByIdOrNull(id) ?: error("Ugyldig primærnøkkel")
    }

    override fun sendTilSak(søknadId: String) {

        val soknad: Soknad = soknadRepository.findByIdOrNull(søknadId) ?: error("")
        val sendTilSakDto = SøknadMapper.fromDto(soknad)
        søknadClient.sendTilSak(sendTilSakDto)
    }

    data class OkDto(val status: String = "OK")


    override fun lagreSøknad(soknad: Soknad) {
        soknadRepository.save(soknad)
    }
}

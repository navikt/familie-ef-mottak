package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.repository.HenvendelseRepository
import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

@Service
class MottakServiceImpl(                        private val arkivClient: ArkivClient,
                        private val henvendelseRepository: HenvendelseRepository) : MottakService {

    @Transactional
    override fun motta(søknadDto: String): Kvittering {

        val henvendelse = Henvendelse(0L, søknadDto)

        henvendelseRepository.save(henvendelse)

        val arkivResponse = arkivClient.arkiver(søknadDto)
        if (arkivResponse.statusCode.is2xxSuccessful) {
            return Kvittering("Søknad mottatt")
        }
        throw HttpServerErrorException(arkivResponse.statusCode, "Arkivering av søknad feilet")
    }

    override fun get(id: Long): Henvendelse {
        val søknad = henvendelseRepository.findById(id)
        return søknad.orElseThrow { HttpClientErrorException(HttpStatus.BAD_REQUEST, "En ugyldig primærnøkkel ble brukt")  }
    }
}
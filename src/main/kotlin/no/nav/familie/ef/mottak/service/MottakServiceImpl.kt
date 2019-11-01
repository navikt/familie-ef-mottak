package no.nav.familie.ef.mottak.service


import io.micrometer.core.instrument.MeterRegistry
import no.nav.familie.ef.mottak.api.dto.Kvittering
//import no.nav.familie.ef.mottak.integration.ArkivClient
//import no.nav.familie.ef.mottak.repository.HenvendelseRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.HttpClientErrorException
//import org.springframework.web.client.HttpServerErrorException

@Service
class MottakServiceImpl(private val registry: MeterRegistry,
                        //private val arkivClient: ArkivClient,
                        //private val henvendelseRepository: HenvendelseRepository,
                         private val søknadRepository: SøknadRepository) : MottakService {

    private val log = LoggerFactory.getLogger(MottakServiceImpl::class.java)

    @Transactional
    override fun motta(søknadDto: String): Kvittering {
        val id = System.currentTimeMillis() //midlertidig

        val søknad = Søknad(id, søknadDto)
        søknadRepository.save(søknad)
        return Kvittering("Søknad lagret med id " + id)
        /*
        val henvendelse = Henvendelse(id, søknadDto)
        henvendelseRepository.save(henvendelse)
        val arkivResponse = arkivClient.arkiver(søknadDto)
        if (arkivResponse.statusCode.is2xxSuccessful) {
            registry.counter("familie.ef.mottak.arkivering.suksess").increment()
            return Kvittering("Søknad mottatt")
        }
        log.error("Arkivering av søknad feilet")
        registry.counter("familie.ef.mottak.arkivering.feil").increment()
        throw HttpServerErrorException(arkivResponse.statusCode, "Arkivering av søknad feilet")
        */

    }

    override fun get(id: Long): Søknad {
        val søknad = søknadRepository.findByIdOrNull(id)
        if (søknad != null) {
            log.error("Hentet søknad med id $id fra arkiv")
            registry.counter("familie.ef.mottak.hent.soknad.suksess").increment()
            return søknad
        } else {
            log.error("Klarte ikke søknad hente med id $id fra arkiv")
            registry.counter("familie.ef.mottak.hent.soknad.feil").increment()
            throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "En ugyldig primærnøkkel ble brukt")
        }
    }

    /*
    override fun get(id: Long): Henvendelse {
        val søknad = henvendelseRepository.findByIdOrNull(id)
        if (søknad != null) {
            log.error("Hentet søknad med id $id fra arkiv")
            registry.counter("familie.ef.mottak.hent.soknad.suksess").increment()
            return søknad
        } else {
            log.error("Klarte ikke søknad hente med id $id fra arkiv")
            registry.counter("familie.ef.mottak.hent.soknad.feil").increment()
            throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "En ugyldig primærnøkkel ble brukt")
        }
    }
    */
}


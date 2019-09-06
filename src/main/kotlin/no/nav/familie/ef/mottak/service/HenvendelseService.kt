package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.repository.HenvendelseRepository
import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import no.nav.familie.ef.mottak.repository.domain.HenvendelseStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HenvendelseService(private val søknadClient: SøknadClient,
                         private val henvendelseRepository: HenvendelseRepository) {

    @Transactional
    fun sendInn(henvendelse: Henvendelse) {
        henvendelseRepository.save(henvendelse.copy(status = HenvendelseStatus.FERDIG))
        søknadClient.sendTilSak(henvendelse.payload)
    }


}
package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class HenvendelseService(private val søknadClient: SøknadClient,
                         private val taskRepository: TaskRepository) {

    @Transactional
    fun sendInn(henvendelse: Henvendelse) {
        taskRepository.save(henvendelse.copy(status = HenvendelseStatus.FERDIG))
        søknadClient.sendTilSak(henvendelse.payload)
    }
}

package no.nav.familie.ef.mottak.service

/*
import no.nav.familie.ef.mottak.repository.HenvendelseRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class ScheduledEventService(private val henvendelseRepository: HenvendelseRepository,
                            private val henvendelseService: HenvendelseService) {

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun sendInn() {
        henvendelseRepository.finnAlleHenvendelserKlareForProsessering().forEach {
            try {
                henvendelseService.sendInn(it)
            } catch (e: Exception) {
                // Log something
            }
        }
    }
}

*/

package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.task.JournalførSøknadTask.Companion.JOURNALFØR_SØKNAD
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class ScheduledEventService(private val taskRepository: TaskRepository,
                            private val soknadRepository: SoknadRepository) {

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForSøknad() {
        soknadRepository.finnAlleSøknaderUtenTask().forEach {
            opprettTask(it)
        }
    }

    @Transactional
    fun opprettTask(soknad: Soknad) {
        taskRepository.save(Task.nyTask(JOURNALFØR_SØKNAD,
                                        soknad.id,
                                        Properties().apply { this["søkersFødselsnummer"] = soknad.fnr }))
    }
}

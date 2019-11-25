package no.nav.familie.ef.mottak.service


import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.TaskRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.task.HentJournalpostIdFraJoarkTask
import no.nav.familie.ef.mottak.task.JournalførSøknadTask
import no.nav.familie.prosessering.domene.Task
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ScheduledEventService(private val taskRepository: TaskRepository,
                            private val søknadRepository: SøknadRepository) {

    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    fun opprettTaskForSøknad() {
        søknadRepository.finnAlleSøknaderUtenTask(PageRequest.of(0, 100)).forEach {
            opprettTask(it)
        }
    }

    @Transactional
    fun opprettTask(søknad: Søknad) {
        val taskType = if (søknad.nySaksbehandling) {
            JournalførSøknadTask.JOURNALFØR_SØKNAD
        } else {
            HentJournalpostIdFraJoarkTask.HENT_JOURNALPOSTID_FRA_JOARK
        }

        val nyTask = Task.nyTask(taskType, søknad.id.toString())
        taskRepository.save(nyTask)

    }

}

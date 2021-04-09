package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.task.LagPdfTask
import no.nav.familie.ef.mottak.task.SendSøknadMottattTilDittNavTask.Companion.TYPE
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class TaskProsesseringService(private val taskRepository: TaskRepository,
                              private val søknadRepository: SøknadRepository) {

    @Transactional
    fun startTaskProsessering(søknad: Søknad) {
        val properties =
                Properties().apply { this["søkersFødselsnummer"] = søknad.fnr }
                        .apply { this["dokumenttype"] = søknad.dokumenttype }
        taskRepository.save(Task(LagPdfTask.LAG_PDF,
                                 søknad.id,
                                 properties))

        taskRepository.save(Task(TYPE,
                                 søknad.id,
                                 properties))
        søknadRepository.save(søknad.copy(taskOpprettet = true))
    }

}


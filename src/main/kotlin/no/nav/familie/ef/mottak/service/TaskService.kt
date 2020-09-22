package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.task.LagPdfTask
import no.nav.familie.ef.mottak.task.SendSøknadMottattTilDittNavTask.Companion.SEND_SØKNAD_MOTTATT_TIL_DITT_NAV
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.domene.TaskRepository
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class TaskService(private val taskRepository: TaskRepository,
                  private val soknadRepository: SoknadRepository) {

    @Transactional
    fun opprettPdfTaskForSoknad(it: Soknad) {
        val properties =
                Properties().apply { this["søkersFødselsnummer"] = it.fnr }.apply { this["dokumenttype"] = it.dokumenttype }
        taskRepository.save(Task.nyTask(LagPdfTask.LAG_PDF,
                                        it.id,
                                        properties))

        soknadRepository.save(it.copy(taskOpprettet = true))
    }

    @Transactional
    fun sendSøknadMottattTilDittNavTask(it: Soknad) {
        val properties =
                Properties().apply { this["søkersFødselsnummer"] = it.fnr }.apply { this["dokumenttype"] = it.dokumenttype }
        taskRepository.save(Task.nyTask(SEND_SØKNAD_MOTTATT_TIL_DITT_NAV,
                                        it.id,
                                        properties))
        soknadRepository.save(it.copy(taskOpprettet = true))
    }

}


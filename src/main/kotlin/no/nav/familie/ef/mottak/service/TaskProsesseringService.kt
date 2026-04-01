package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.task.LagEttersendingPdfTask
import no.nav.familie.ef.mottak.task.LagSøknadPdfTask
import no.nav.familie.ef.mottak.task.SendSøknadMottattTilDittNavTask
import no.nav.familie.log.IdUtils
import no.nav.familie.log.mdc.MDCConstants
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.internal.TaskService
import org.jboss.logging.MDC
import org.slf4j.LoggerFactory
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Properties
import java.util.UUID

@Service
class TaskProsesseringService(
    private val taskService: TaskService,
    private val søknadRepository: SøknadRepository,
    private val entityOperations: JdbcAggregateOperations,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun startTaskProsessering(søknad: Søknad) {
        val properties =
            Properties()
                .apply { this["søkersFødselsnummer"] = søknad.fnr }
                .apply { this["dokumenttype"] = søknad.dokumenttype }
        taskService.save(
            Task(
                LagSøknadPdfTask.TYPE,
                søknad.id,
                properties,
            ),
        )

        properties["eventId"] = UUID.randomUUID().toString()
        taskService.save(
            Task(
                SendSøknadMottattTilDittNavTask.TYPE,
                søknad.id,
                properties,
            ),
        )
        søknadRepository.update(søknad.copy(taskOpprettet = true))
        logger.info("PdfKvitteringTask opprettet for søknad med id ${søknad.id}")
    }

    @Transactional
    fun startTaskProsessering(ettersending: Ettersending) {
        val properties =
            Properties().apply {
                this["søkersFødselsnummer"] = ettersending.fnr
                this["stønadType"] = ettersending.stønadType
            }

        val task = Task(LagEttersendingPdfTask.TYPE, ettersending.id.toString(), properties)

        task.metadata.apply {
            this["callId"] = hentEllerOpprettCallId() + "_" + ettersending.stønadType.replace("Ø", "O")
        }

        taskService.save(task)
        entityOperations.update(ettersending.copy(taskOpprettet = true))
    }

    private fun hentEllerOpprettCallId(): String = MDC.get(MDCConstants.MDC_CALL_ID) as? String ?: IdUtils.generateId()
}

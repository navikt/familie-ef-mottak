package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.EttersendingService
import no.nav.familie.ef.mottak.task.ArkiverEttersendingTask
import no.nav.familie.ef.mottak.task.LagJournalføringsoppgaveForEttersendingTask
import no.nav.familie.log.IdUtils
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(path = ["/api/forvaltning"])
@ProtectedWithClaims(issuer = "azuread")
class ForvaltningController(
    private val ettersendingService: EttersendingService,
    private val taskService: TaskService,
) {
    @PostMapping("/ettersending/splitt")
    fun trekkUtVedleggFraEttersending(
        @RequestBody ettersendingVedleggId: EttersendingVedleggId,
    ): ResponseEntity<String> {
        val nyEttersendingId = ettersendingService.trekkUtEttersendingTilEgenTaskForVedlegg(ettersendingVedleggId.id)
        return ResponseEntity.ok("Opprettet ny ettersending med id: $nyEttersendingId")
    }

    @PostMapping("/ettersending/nycallid")
    fun settNyCallIdPåTaskForEttersending(
        @RequestBody taskId: TaskId,
    ): ResponseEntity<String> {
        val task = taskService.findById(taskId.id)
        require(task.type == ArkiverEttersendingTask.TYPE)
        require(task.status == Status.FEILET || task.status == Status.MANUELL_OPPFØLGING) {
            "Kan ikke legge på ny callId på task når status er ${task.status}"
        }
        val generateId = IdUtils.generateId()
        task.metadata.apply {
            this["callId"] = generateId
        }
        taskService.save(task)
        return ResponseEntity.ok("Endret callId på task til: $generateId")
    }

    @PostMapping("/ettersending/journalforingoppgave/endrecallid")
    fun endreCallIdPåTaskForJournalføringoppgave(
        @RequestBody taskId: TaskId,
    ): ResponseEntity<String> {
        val task = taskService.findById(taskId.id)
        require(
            task.type == LagJournalføringsoppgaveForEttersendingTask.TYPE,
        ) { "Kan ikke endre på ny callId på task når type er ${task.type}" }
        require(task.status == Status.FEILET) { "Kan ikke endre på ny callId på task når status er ${task.status}" }
        val callId = task.callId.replace("Ø", "O")
        task.metadata.apply {
            this["callId"] = callId
        }
        taskService.save(task)
        return ResponseEntity.ok("Endret callId på task til: $callId")
    }
}

data class TaskId(
    val id: Long,
)

data class EttersendingVedleggId(
    val id: UUID,
)

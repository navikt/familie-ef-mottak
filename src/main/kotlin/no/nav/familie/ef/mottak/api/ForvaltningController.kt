package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.EttersendingService
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
class ForvaltningController(private val ettersendingService: EttersendingService) {

    @PostMapping("/ettersending/splitt")
    fun trekkUtVedleggFraEttersending(@RequestBody ettersendingVedleggId: EttersendingVedleggId): ResponseEntity<String> {
        val nyEttersendingId = ettersendingService.trekkUtEttersendingTilEgenTaskForVedlegg(ettersendingVedleggId.id)
        return ResponseEntity.ok("Opprettet ny ettersending med id: $nyEttersendingId")
    }
}

data class EttersendingVedleggId(
    val id: UUID,
)

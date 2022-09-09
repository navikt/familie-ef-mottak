package no.nav.familie.ef.mottak.mockapi

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping(path = ["/mockintegrasjoner/"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class MockIntegrasjonerController {

    @PostMapping(path = ["/arkiv/v4"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun arkiverDokument(
        @RequestBody @Valid
        arkiverDokumentRequest: ArkiverDokumentRequest
    ): ResponseEntity<Ressurs<ArkiverDokumentResponse>> {
        val data = ArkiverDokumentResponse("JOURNALPOST_MOCK_ID", true)
        val ressurs: Ressurs<ArkiverDokumentResponse> = success(data)
        return ResponseEntity.status(HttpStatus.CREATED).body(ressurs)
    }

    @GetMapping("/journalpost/sak")
    @Unprotected
    fun hentSakId(@RequestParam(name = "journalpostId") journalpostId: String): ResponseEntity<Ressurs<Map<String, String>>> {
        val saksnummer = UUID.randomUUID().toString()
        return ResponseEntity.ok(success(mapOf("saksnummer" to saksnummer), "OK"))
    }
}

package no.nav.familie.ef.mottak.mockapi

//import no.nav.familie.kontrakter.felles.Ressurs
//import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.ef.mottak.integration.dto.ArkiverDokumentResponse
import no.nav.familie.ef.mottak.integration.dto.ArkiverSøknadRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*
import javax.validation.Valid

//class MinRes() : Ressurs<ArkiverDokumentResponse>(){}

@RestController
@RequestMapping(path = ["/mockapi/"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class MockController() {

    data class OkDto(val status: String = "OK")

    @PostMapping(path = ["/arkiv/v2"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun arkiverDokument(@RequestBody @Valid
                        arkiverDokumentRequest: ArkiverSøknadRequest): ResponseEntity<Ressurs<ArkiverDokumentResponse>> {
        val data = ArkiverDokumentResponse("JOURNALPOST_MOCK_ID", true)
        val ressurs: Ressurs<ArkiverDokumentResponse> = success(data)
        return ResponseEntity.status(HttpStatus.CREATED).body(ressurs)
    }

    @PostMapping("/mottak/dokument")
    @Unprotected
    fun mockMottakDokument(): ResponseEntity<OkDto> {
        return ResponseEntity.ok(OkDto())
    }

    @GetMapping("/journalpost/kanalreferanseid/{id}")
    @Unprotected
    fun mockKanalRef(@PathVariable id: String): String {
        return UUID.randomUUID().toString()
    }


    @GetMapping("/journalpost/{id}/sak")
    @Unprotected
    fun mockSak(@PathVariable id: String): String {
        return UUID.randomUUID().toString()
    }


    @PostMapping("/saktoken")
    @Unprotected
    fun mockOauthToken(): ResponseEntity<OAuth2AccessTokenResponse> {
        return ResponseEntity.ok(OAuth2AccessTokenResponse.builder().accessToken(tokenResponse()).build())
    }

    @PostMapping("/integrationtoken")
    @Unprotected
    fun mockIntOauthToken(): ResponseEntity<OAuth2AccessTokenResponse> {
        return ResponseEntity.ok(OAuth2AccessTokenResponse.builder().accessToken(tokenResponse()).build())
    }

    private fun tokenResponse(): String {
        return token
                .replace("###expires_at###", "" + Instant.now().plusSeconds(3600).epochSecond)
    }

    // language=jSon
    val token: String =
            """{"token_type": "Bearer","scope":"###expires_at###","expires_at":"28021078036","ext_expires_in":"30","expires_in":"30","access_token":"somerandomaccesstoken"}"""

}



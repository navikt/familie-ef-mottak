package no.nav.familie.ef.mottak.mockapi

//import okhttp3.mockwebserver.MockResponse

import no.nav.familie.ef.mottak.integration.dto.ArkiverDokumentResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*

@RestController
@RequestMapping(path = ["/mockapi/"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class MockController() {

    data class OkDto(val status: String = "OK")

    @PostMapping("/arkiv")
    @Unprotected
    fun mockPost(): ArkiverDokumentResponse {
        return ArkiverDokumentResponse("OK", true)
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

    @PostMapping("/oauth2/v2.0/token")
    @Unprotected
    fun mockToken(): ResponseEntity<String> {
        return ResponseEntity.ok(tokenResponse())
    }

    private fun tokenResponse(): String {
        return token
                .replace("###expires_at###", "" + Instant.now().plusSeconds(3600).epochSecond)
    }

    // language=jSon
    val token: String =
            """{"token_type": "Bearer","scope":"###expires_at###","expires_at":"28021078036","ext_expires_in":"30","expires_in":"30","access_token":"somerandomaccesstoken"}"""

}



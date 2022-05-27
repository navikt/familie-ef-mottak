package no.nav.familie.ef.mottak.mockapi

// import no.nav.familie.kontrakter.felles.Ressurs
// import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping(path = ["/mockapi/"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class MockController {

    data class OkDto(val status: String = "OK")

    @PostMapping("/mottak/dokument")
    @Unprotected
    fun mockMottakDokument(): ResponseEntity<OkDto> {
        return ResponseEntity.ok(OkDto())
    }

    @PostMapping("/token")
    @Unprotected
    fun mockOauthToken(): ResponseEntity<OAuth2AccessTokenResponse> {
        return ResponseEntity.ok(OAuth2AccessTokenResponse.builder().accessToken(tokenResponse()).build())
    }

    private fun tokenResponse(): String {
        return token
            .replace("###expires_at###", "" + Instant.now().plusSeconds(3600).epochSecond)
    }

    @Suppress("LongLine")
    // language=jSon
    val token: String =
        """{"token_type": "Bearer","scope":"###expires_at###","expires_at":"28021078036","ext_expires_in":"30","expires_in":"30","access_token":"somerandomaccesstoken"}"""
}

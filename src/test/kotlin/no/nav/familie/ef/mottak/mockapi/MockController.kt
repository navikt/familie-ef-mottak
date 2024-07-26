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

@RestController
@RequestMapping(path = ["/mockapi/"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Unprotected
class MockController {
    data class OkDto(
        val status: String = "OK",
    )

    @PostMapping("/mottak/dokument")
    @Unprotected
    fun mockMottakDokument(): ResponseEntity<OkDto> = ResponseEntity.ok(OkDto())

    @PostMapping("/token")
    @Unprotected
    fun mockOauthToken(): ResponseEntity<OAuth2AccessTokenResponse> = ResponseEntity.ok(OAuth2AccessTokenResponse("Mock-token-response", 60, 60, emptyMap()))
}

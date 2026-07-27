package no.nav.familie.ef.mottak.mockapi

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/mockapi/"], produces = [MediaType.APPLICATION_JSON_VALUE])
class MockController {
    data class OkDto(
        val status: String = "OK",
    )

    data class MockTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String = "mock-token-response",
        @JsonProperty("expires_in")
        val utløperOm: Int = 3600,
        @JsonProperty("token_type")
        val tokenType: String = "Bearer",
    )

    @PostMapping("/mottak/dokument")
    fun mockMottakDokument(): ResponseEntity<OkDto> = ResponseEntity.ok(OkDto())

    @PostMapping("/token")
    fun mockOauthToken(): ResponseEntity<MockTokenResponse> = ResponseEntity.ok(MockTokenResponse())
}

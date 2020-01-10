package no.nav.familie.ef.mottak.config.interceptor

import no.nav.familie.ef.mottak.config.ClientConfigurationPropertiesLocal
import no.nav.familie.ef.mottak.config.ClientPropertiesMedUrl
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.net.URI

@Component
class BearerTokenClientInterceptor(private val oAuth2AccessTokenService: OAuth2AccessTokenService,
                                   private val clientConfigurationPropertiesLocal: ClientConfigurationPropertiesLocal) :
        ClientHttpRequestInterceptor {


    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        val clientProperties = clientPropertiesFor(request.uri)
        val response: OAuth2AccessTokenResponse = oAuth2AccessTokenService.getAccessToken(clientProperties)
        request.headers.setBearerAuth(response.accessToken)
        return execution.execute(request, body)
    }

    private fun clientPropertiesFor(uri: URI): ClientPropertiesMedUrl {
        return clientConfigurationPropertiesLocal
                       .registration
                       .values
                       .firstOrNull { uri.toString().startsWith(it.resourceUrl.toString()) }
               ?: error("could not find oauth2 client config for uri=$uri")
    }

}

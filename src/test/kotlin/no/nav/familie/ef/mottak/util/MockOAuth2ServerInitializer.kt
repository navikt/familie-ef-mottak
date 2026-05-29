package no.nav.familie.ef.mottak.util

import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils

class MockOAuth2ServerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val azureIssuerUrl = mockOAuth2Server.issuerUrl(AZURE_ISSUER_ID).toString().trimEnd('/')
        val tokenxIssuerUrl = mockOAuth2Server.issuerUrl(TOKENX_ISSUER_ID).toString().trimEnd('/')
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "AZURE_OPENID_CONFIG_ISSUER=$azureIssuerUrl",
            "AZURE_APP_CLIENT_ID=$AUDIENCE",
            "TOKEN_X_ISSUER=$tokenxIssuerUrl",
            "TOKEN_X_CLIENT_ID=$AUDIENCE",
        )
    }

    companion object {
        const val AZURE_ISSUER_ID = "azuread"
        const val TOKENX_ISSUER_ID = "tokenx"
        const val AUDIENCE = "aud-localhost"

        val mockOAuth2Server: MockOAuth2Server by lazy {
            MockOAuth2Server().apply { start() }
        }
    }
}

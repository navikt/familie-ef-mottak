package no.nav.familie.ef.mottak.config

import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import no.nav.security.token.support.client.core.ClientAuthenticationProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.OAuth2GrantType
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URI


@ConstructorBinding
class ClientPropertiesMedUrl(val resourceUrl: URI,
                             tokenEndpointUrl: URI,
                             grantType: OAuth2GrantType,
                             scope: List<String>,
                             authentication: ClientAuthenticationPropertiesLocal)

    : ClientProperties(tokenEndpointUrl,
                       grantType,
                       scope,
                       authentication) {

    @ConstructorBinding
    class ClientAuthenticationPropertiesLocal(clientId: String,
                                              clientAuthMethod: ClientAuthenticationMethod,
                                              clientSecret: String) : ClientAuthenticationProperties(clientId,
                                                                                                     clientAuthMethod,
                                                                                                     clientSecret,
                                                                                                     null)

}


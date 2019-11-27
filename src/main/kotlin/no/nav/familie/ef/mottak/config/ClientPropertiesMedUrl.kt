package no.nav.familie.ef.mottak.config

import no.nav.security.token.support.client.core.ClientAuthenticationProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.OAuth2GrantType
import java.net.URI


class ClientPropertiesMedUrl(val resourceUrl:URI,
                             tokenEndpointUrl: URI,
                             grantType: OAuth2GrantType,
                             scope: List<String>,
                             authentication: ClientAuthenticationProperties)

    : ClientProperties(tokenEndpointUrl,
                       grantType,
                       scope,
                       authentication)

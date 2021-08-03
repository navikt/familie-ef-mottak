package no.nav.familie.ef.mottak.integration.health

import no.nav.familie.ef.mottak.integration.SaksbehandlingClient
import no.nav.familie.http.health.AbstractHealthIndicator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
@Profile("!local")
class SaksbehandlingHealth(client: SaksbehandlingClient) : AbstractHealthIndicator(client, "saksbehandling")

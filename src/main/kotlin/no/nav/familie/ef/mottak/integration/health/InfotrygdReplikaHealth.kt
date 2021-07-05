package no.nav.familie.ef.mottak.integration.health

import no.nav.familie.ef.mottak.integration.InfotrygdReplikaClient
import no.nav.familie.http.health.AbstractHealthIndicator
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
@Profile("!local")
class InfotrygdReplikaHealth(client: InfotrygdReplikaClient) : AbstractHealthIndicator(client, "infotrygd.replika")

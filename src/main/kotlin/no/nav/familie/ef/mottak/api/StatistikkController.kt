package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.repository.StatistikkRepository
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/statistikk"])
@ProtectedWithClaims(issuer = "azuread")
class StatistikkController(private val statistikkRepository: StatistikkRepository) {

    val minSize = listOf(DOKUMENTTYPE_OVERGANGSSTØNAD,
                         DOKUMENTTYPE_BARNETILSYN,
                         DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                         DOKUMENTTYPE_SKOLEPENGER).maxOf { it.length }

    @GetMapping("soknader", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun søknader(): String {
        return statistikkRepository.antallSøknaderPerDokumentType()
                .sortedWith(compareBy({ it.dato }, { it.type }))
                .joinToString("\n") { "${it.dato} ${String.format("%-${minSize}s", it.type)} ${it.antall}" }
    }

}

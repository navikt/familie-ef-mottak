package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDateTime

@RestController
@RequestMapping(path = ["/api/status"], produces = [MediaType.APPLICATION_JSON_VALUE])
class StatusController(val søknadRepository: SøknadRepository) {

    val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping()
    @Unprotected
    fun status(): StatusDto {
        val søknad = søknadRepository.finnSisteLagredeSøknad()
        val tidSidenSisteLagredeSøknad = Duration.between(søknad.opprettetTid, LocalDateTime.now())
        loggLiteAktivitet(tidSidenSisteLagredeSøknad)
        return statusDto(tidSidenSisteLagredeSøknad)
    }

    private fun loggLiteAktivitet(tidSidenSisteLagredeSøknad: Duration) {
        if (erDagtid() && !erHelg()) {
            when {
                tidSidenSisteLagredeSøknad.toHours() > 3 -> logger.error("Status ef-mottak: Det er ${tidSidenSisteLagredeSøknad.toHours()} timer siden vi mottok en søknad")
                tidSidenSisteLagredeSøknad.toMinutes() > 20 -> logger.warn("Status ef-mottak: Det er ${tidSidenSisteLagredeSøknad.toMinutes()} siden vi mottok en søknad")
            }
        }
    }

    private fun erHelg() = LocalDateTime.now().dayOfWeek.value in 6..7

    private fun statusDto(tidSidenSisteLagredeSøknad: Duration) = when {
        erTidspunktMedForventetAktivitet() -> dagStatus(tidSidenSisteLagredeSøknad)
        else -> nattStatus(tidSidenSisteLagredeSøknad)
    }

    private fun nattStatus(tidSidenSisteLagredeSøknad: Duration) =
        when {
            tidSidenSisteLagredeSøknad.toHours() > 24 -> StatusDto(
                status = Plattformstatus.ISSUE,
                description = "Det er over 24 timer siden vi mottok en søknad",
            )
            else -> StatusDto(status = Plattformstatus.OK, description = "Alt er bra", logLink = null)
        }

    private fun dagStatus(tidSidenSisteLagredeSøknad: Duration) =
        when {
            tidSidenSisteLagredeSøknad.toHours() > 12 -> StatusDto(
                status = Plattformstatus.ISSUE,
                description = "Det er over 12 timer siden vi mottok en søknad",
            )
            else -> StatusDto(status = Plattformstatus.OK, description = "Alt er bra", logLink = null)
        }
}

const val LOG_URL = "https://logs.adeo.no/app/discover#/view/a3e93b80-c1a5-11ee-a029-75a0ed43c092?_g=()"

data class StatusDto(val status: Plattformstatus, val description: String? = null, val logLink: String? = LOG_URL)

// OK, ISSUE, DOWN
enum class Plattformstatus {
    OK, ISSUE
}

fun erTidspunktMedForventetAktivitet() = LocalDateTime.now().hour in 12..21
fun erDagtid() = LocalDateTime.now().hour in 9..22

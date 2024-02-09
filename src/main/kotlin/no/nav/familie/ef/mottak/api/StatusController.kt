package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDateTime

@RestController
@RequestMapping(path = ["/api/status"], produces = [MediaType.APPLICATION_JSON_VALUE])
class StatusController(val søknadRepository: SøknadRepository) {

    @GetMapping()
    @Unprotected
    fun status(): StatusDto {
        val søknad = søknadRepository.finnSisteLagredeSøknad()
        val tidSidenSisteLagredeSøknad = Duration.between(LocalDateTime.now(), søknad.opprettetTid)

        when {
            erNatt() -> {
                return StatusDto(status = Plattformstatus.OK, description = "Alt er bra", logLink = null)
            }

            tidSidenSisteLagredeSøknad.toHours() > 24 -> {
                return StatusDto(
                    status = Plattformstatus.DOWN,
                    description = "Det er over 24 timer siden vi mottok en søknad",
                )
            }

            tidSidenSisteLagredeSøknad.toHours() > 5 -> {
                return StatusDto(
                    status = Plattformstatus.ISSUE,
                    description = "Det er over 5 timer siden vi mottok en søknad",
                )
            }

            tidSidenSisteLagredeSøknad.toHours() > 1 -> {
                return StatusDto(
                    status = Plattformstatus.ISSUE,
                    description = "Det er over 1 time siden vi mottok en søknad",
                )
            }

            tidSidenSisteLagredeSøknad.toMinutes() > 20 -> {
                return StatusDto(
                    status = Plattformstatus.ISSUE,
                    description = "Det er over 20 minutter siden siste søknad ble mottatt",
                )
            }

            else -> {
                return StatusDto(status = Plattformstatus.OK, description = "Alt er bra", logLink = null)
            }
        }
    }
}

const val LOG_URL = "https://logs.adeo.no/app/discover#/view/a3e93b80-c1a5-11ee-a029-75a0ed43c092?_g=()"

data class StatusDto(val status: Plattformstatus, val description: String? = null, val logLink: String? = LOG_URL)

enum class Plattformstatus {
    OK, ISSUE, DOWN
}

fun erNatt() = LocalDateTime.now().hour !in 8..21

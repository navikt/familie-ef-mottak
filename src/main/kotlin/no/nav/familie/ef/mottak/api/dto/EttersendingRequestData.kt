package no.nav.familie.ef.mottak.api.dto
import no.nav.familie.kontrakter.felles.PersonIdent
import java.time.LocalDateTime

data class EttersendingRequestData(
        val datoMottat: LocalDateTime,
        val filnavn: PersonIdent
)

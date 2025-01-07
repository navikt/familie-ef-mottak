package no.nav.familie.ef.mottak.repository.domain

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotNull

data class FeltMap(
    @field:NotNull(message = "Label kan ikke være null")
    val label: String,
    @field:NotNull(message = "Verdiliste kan ikke være null")
    val verdiliste: List<VerdilisteElement>,
    val PdfConfig: PdfConfig? = null,
)
enum class Sprak {
    NO,
    EN,
}

data class PdfConfig(
    val harInnholdsFortegnelse: Boolean,
    val sprak: Sprak? = null,
)


@JsonInclude(JsonInclude.Include.NON_NULL)
data class VerdilisteElement(
    val label: String,
    val visningsVariant: String? = null,
    val verdi: String? = null,
    val verdiliste: List<VerdilisteElement>? = null,
    val alternativer: String? = null,
)

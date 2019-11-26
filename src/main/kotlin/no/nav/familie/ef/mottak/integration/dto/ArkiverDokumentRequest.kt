package no.nav.familie.ef.mottak.integration.dto

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

data class ArkiverDokumentRequest(@NotBlank
                                  val fnr: String,
                                  val forsøkFerdigstill: Boolean,
                                  @NotEmpty
                                  val dokumenter: List<Dokument>)

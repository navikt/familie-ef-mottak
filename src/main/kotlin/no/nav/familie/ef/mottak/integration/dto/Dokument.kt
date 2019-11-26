package no.nav.familie.ef.mottak.integration.dto

import javax.validation.constraints.NotEmpty

class Dokument(@NotEmpty
               val dokument: ByteArray,
               @NotEmpty
               val filtype: Filtype,
               @NotEmpty
               val dokumentType: DokumentType,
               val filnavn: String? = null)

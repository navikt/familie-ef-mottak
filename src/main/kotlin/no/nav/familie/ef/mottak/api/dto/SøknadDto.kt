package no.nav.familie.ef.mottak.api.dto

import javax.validation.constraints.NotEmpty

class SøknadDto(@NotEmpty val fnr: String,
                @NotEmpty val soknad: String,
                @NotEmpty val soknadPdf: ByteArray,
                val vedlegg: List<VedleggDto>)

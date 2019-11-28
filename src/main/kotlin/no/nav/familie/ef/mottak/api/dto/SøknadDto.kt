package no.nav.familie.ef.mottak.api.dto

import javax.validation.constraints.NotEmpty

class SøknadDto(val fnr: String,
                val soknadJson: String,
                val soknadPdf: String,
                val vedlegg: List<VedleggDto>)

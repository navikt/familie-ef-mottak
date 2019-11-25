package no.nav.familie.ef.mottak.api.dto

data class SøknadDto(val fnr: String,
                     val soknad: String,
                     val vedlegg: List<VedleggDto>)
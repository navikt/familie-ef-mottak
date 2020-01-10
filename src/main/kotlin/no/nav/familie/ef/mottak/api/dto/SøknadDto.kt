package no.nav.familie.ef.mottak.api.dto

class SøknadDto(val fnr: String,
                val soknadJson: String,
                val soknadPdf: String,
                val vedlegg: List<VedleggDto>)

package no.nav.familie.ef.mottak.personopplysninger

data class PdlIdentRequestVariables(
    val ident: String,
    val gruppe: String,
    val historikk: Boolean = false,
)

package no.nav.familie.ef.mottak.personopplysninger

open class PdlRequestException(
    melding: String? = null,
) : Exception(melding)

class PdlNotFoundException : PdlRequestException()

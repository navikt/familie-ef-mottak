package no.nav.familie.ef.mottak.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Søknad med etterspurt ID ikke funnet")
class SøknadNotFoundException : RuntimeException()

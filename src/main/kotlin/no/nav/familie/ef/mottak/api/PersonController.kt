package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(consumes = [APPLICATION_JSON_VALUE], path = ["/api/person"], produces = [APPLICATION_JSON_VALUE])
@Protected
class PersonController(val søknadService: SøknadService) {

    @PostMapping("soknader")
    fun søknaderForPerson(@RequestBody personIdent: PersonIdent): ResponseEntity<List<String>> {

        return ResponseEntity.ok().body(søknadService.hentSøknaderForPerson(personIdent.ident))
    }

}

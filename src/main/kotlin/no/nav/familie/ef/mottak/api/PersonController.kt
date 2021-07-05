package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.ef.mottak.util.getRootCause
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping(consumes = [MULTIPART_FORM_DATA_VALUE], path = ["/api/person"], produces = [APPLICATION_JSON_VALUE])
@Protected
class PersonController(val søknadService: SøknadService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("soknader")
    fun søknaderForPerson(@RequestBody personIdent: PersonIdent): ResponseEntity<List<String>> {

        return ResponseEntity.ok().body(søknadService.hentSøknaderForPerson(personIdent.ident))
    }

}

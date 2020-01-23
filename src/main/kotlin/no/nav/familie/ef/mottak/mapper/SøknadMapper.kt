package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.ef.søknad.Søknad as Kontraktssøknad

object SøknadMapper {

    fun toDto(soknad: Soknad): Kontraktssøknad {
        return objectMapper.readValue(soknad.søknadJson)
    }

    fun fromDto(kontraktssøknad: Kontraktssøknad): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(kontraktssøknad),
                      fnr = kontraktssøknad.personalia.verdi.fødselsnummer.verdi.verdi)
    }
}

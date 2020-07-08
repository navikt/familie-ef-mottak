package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.BarnetilsynSøknadKontrakt
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYNSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.ef.søknad.Søknad as Kontraktssøknad

object SøknadMapper {

    inline fun <reified T : Any> toDto(soknad: Soknad): T {
        return objectMapper.readValue(soknad.søknadJson)
    }

    fun fromDto(kontraktssøknad: Kontraktssøknad): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(kontraktssøknad),
                      fnr = kontraktssøknad.personalia.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                      vedlegg = null)
    }

    fun fromDto(skjemaForArbeidssøker: SkjemaForArbeidssøker): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(skjemaForArbeidssøker),
                      fnr = skjemaForArbeidssøker.personaliaArbeidssøker.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                      vedlegg = null)
    }

    fun fromDto(søknadOmBarnetilsyn: BarnetilsynSøknadKontrakt): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(søknadOmBarnetilsyn),
                      fnr = søknadOmBarnetilsyn.personalia.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_BARNETILSYNSTØNAD,
                      vedlegg = null)
    }
}

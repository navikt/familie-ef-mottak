package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg as Kontraktssøknad

object SøknadMapper {

    inline fun <reified T : Any> toDto(soknad: Soknad): T {
        return objectMapper.readValue(soknad.søknadJson)
    }

    fun fromDto(kontraktssøknad: Kontraktssøknad): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(kontraktssøknad.søknad),
                      fnr = kontraktssøknad.søknad.personalia.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                      vedlegg = objectMapper.writeValueAsString(kontraktssøknad.vedlegg))
    }

    fun fromDto(skjemaForArbeidssøker: SkjemaForArbeidssøker): Soknad {
        return Soknad(søknadJson = objectMapper.writeValueAsString(skjemaForArbeidssøker),
                      fnr = skjemaForArbeidssøker.personaliaArbeidssøker.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                      vedlegg = null)
    }
}

package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.felles.objectMapper

object SøknadMapper {

    inline fun <reified T : Any> toDto(søknad: Søknad): T {
        return objectMapper.readValue(søknad.søknadJson)
    }

    fun fromDto(søknad: SøknadOvergangsstønad, skalAutomatiskJournalføres: Boolean): Søknad {
        return Søknad(søknadJson = objectMapper.writeValueAsString(søknad),
                      fnr = søknad.personalia.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
                      skalAutomatiskJournalføres = skalAutomatiskJournalføres)
    }

    fun fromDto(søknad: SøknadBarnetilsyn, skalAutomatiskJournalføres: Boolean): Søknad {
        return Søknad(søknadJson = objectMapper.writeValueAsString(søknad),
                      fnr = søknad.personalia.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_BARNETILSYN,
                      skalAutomatiskJournalføres = skalAutomatiskJournalføres)
    }

    fun fromDto(skjemaForArbeidssøker: SkjemaForArbeidssøker): Søknad {
        return Søknad(søknadJson = objectMapper.writeValueAsString(skjemaForArbeidssøker),
                      fnr = skjemaForArbeidssøker.personaliaArbeidssøker.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                      skalAutomatiskJournalføres = true)
    }

    fun fromDto(søknadSkolepenger: SøknadSkolepenger, skalAutomatiskJournalføres: Boolean): Søknad {
        return Søknad(søknadJson = objectMapper.writeValueAsString(søknadSkolepenger),
                      fnr = søknadSkolepenger.personalia.verdi.fødselsnummer.verdi.verdi,
                      dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
                      skalAutomatiskJournalføres = skalAutomatiskJournalføres)
    }

}

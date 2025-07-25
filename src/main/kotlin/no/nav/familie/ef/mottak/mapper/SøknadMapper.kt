package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.felles.objectMapper

object SøknadMapper {
    inline fun <reified T : Any> toDto(søknad: Søknad): T = objectMapper.readValue(søknad.søknadJsonSafe())

    fun fromDto(
        søknad: SøknadOvergangsstønad,
        behandleINySaksbehandling: Boolean,
    ): Søknad {
        val data = objectMapper.writeValueAsString(søknad)
        return Søknad(
            søknadJson = EncryptedString(data),
            fnr = søknad.personalia.verdi.fødselsnummer.verdi.verdi,
            dokumenttype = DOKUMENTTYPE_OVERGANGSSTØNAD,
            behandleINySaksbehandling = behandleINySaksbehandling,
            json = data,
        )
    }

    fun fromDto(
        søknad: SøknadBarnetilsyn,
        behandleINySaksbehandling: Boolean,
    ): Søknad {
        val data = objectMapper.writeValueAsString(søknad)
        return Søknad(
            søknadJson = EncryptedString(data),
            fnr = søknad.personalia.verdi.fødselsnummer.verdi.verdi,
            dokumenttype = DOKUMENTTYPE_BARNETILSYN,
            behandleINySaksbehandling = behandleINySaksbehandling,
            json = data,
        )
    }

    fun fromDto(skjemaForArbeidssøker: SkjemaForArbeidssøker): Søknad {
        val data = objectMapper.writeValueAsString(skjemaForArbeidssøker)
        return Søknad(
            søknadJson = EncryptedString(data),
            fnr = skjemaForArbeidssøker.personaliaArbeidssøker.verdi.fødselsnummer.verdi.verdi,
            dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
            behandleINySaksbehandling = false,
            json = data,
        )
    }

    fun fromDto(
        søknadSkolepenger: SøknadSkolepenger,
        behandleINySaksbehandling: Boolean,
    ): Søknad {
        val data = objectMapper.writeValueAsString(søknadSkolepenger)
        return Søknad(
            søknadJson = EncryptedString(data),
            fnr = søknadSkolepenger.personalia.verdi.fødselsnummer.verdi.verdi,
            dokumenttype = DOKUMENTTYPE_SKOLEPENGER,
            behandleINySaksbehandling = behandleINySaksbehandling,
            json = data,
        )
    }
}

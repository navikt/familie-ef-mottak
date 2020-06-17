package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg

interface SøknadService {

    fun motta(søknad: SøknadMedVedlegg): Kvittering

    fun get(id: String): Soknad

    fun sendTilSak(søknadId: String)

    fun lagreSøknad(soknad: Soknad)

    fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering
}

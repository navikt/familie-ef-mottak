package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.*

interface SøknadService {

    fun mottaOvergangsstønad(søknad: SøknadMedVedlegg<SøknadOvergangsstønad>, vedlegg: Map<String, ByteArray>): Kvittering

    fun mottaBarnetilsyn(søknad: SøknadMedVedlegg<SøknadBarnetilsyn>, vedlegg: Map<String, ByteArray>): Kvittering

    fun mottaSkolepenger(søknad: SøknadMedVedlegg<SøknadSkolepenger>, vedlegg: Map<String, ByteArray>): Kvittering

    fun get(id: String): Soknad

    fun sendTilSak(søknadId: String)

    fun lagreSøknad(soknad: Soknad)

    fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering
}

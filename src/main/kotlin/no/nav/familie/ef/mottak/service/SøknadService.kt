package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.SøknadBarnetilsyn
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg

interface SøknadService {

    fun motta(søknad: SøknadMedVedlegg, vedlegg: Map<String, ByteArray>): Kvittering

    fun get(id: String): Soknad

    fun sendTilSak(søknadId: String)

    fun lagreSøknad(soknad: Soknad)

    fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering

    fun motta(søknad: SøknadBarnetilsyn, vedlegg: Map<String?, ByteArray>): Kvittering

}

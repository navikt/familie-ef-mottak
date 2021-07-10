package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import java.util.*

interface SøknadService {

    fun mottaOvergangsstønad(søknad: SøknadMedVedlegg<SøknadOvergangsstønad>, vedlegg: Map<String, ByteArray>): Kvittering

    fun mottaBarnetilsyn(søknad: SøknadMedVedlegg<SøknadBarnetilsyn>, vedlegg: Map<String, ByteArray>): Kvittering

    fun mottaSkolepenger(søknad: SøknadMedVedlegg<SøknadSkolepenger>, vedlegg: Map<String, ByteArray>): Kvittering

    fun get(id: String): Søknad

    fun lagreSøknad(søknad: Søknad)

    fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering

    fun hentDokumentasjonsbehovForSøknad(søknadId: UUID): DokumentasjonsbehovDto
}

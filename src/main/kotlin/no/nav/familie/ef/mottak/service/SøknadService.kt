package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.kontrakter.felles.PersonIdent
import java.util.*

interface SøknadService {

    fun mottaOvergangsstønad(søknad: SøknadMedVedlegg<SøknadOvergangsstønad>, vedlegg: Map<String, ByteArray>): Kvittering

    fun mottaBarnetilsyn(søknad: SøknadMedVedlegg<SøknadBarnetilsyn>, vedlegg: Map<String, ByteArray>): Kvittering

    fun mottaSkolepenger(søknad: SøknadMedVedlegg<SøknadSkolepenger>, vedlegg: Map<String, ByteArray>): Kvittering

    fun get(id: String): Soknad

    fun lagreSøknad(soknad: Soknad)

    fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering

    fun hentDokumentasjonsbehovForSøknad(søknadId: UUID): DokumentasjonsbehovDto

    fun hentSøknaderForPerson(personIdent: String): List<String>
}

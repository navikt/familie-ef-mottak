package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Barn
import no.nav.familie.kontrakter.ef.søknad.Utenlandsopphold

sealed class SøknadsfeltType {
    data class BarnElement(
        val barn: Barn,
    ) : SøknadsfeltType()

    data class UtenlandsoppholdElement(
        val utenlandsopphold: Utenlandsopphold,
    ) : SøknadsfeltType()

    data class ArbeidsforholdElement(
        val arbeidsforhold: no.nav.familie.kontrakter.ef.søknad.Arbeidsgiver,
    ) : SøknadsfeltType()
}

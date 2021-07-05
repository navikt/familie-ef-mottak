package no.nav.familie.ef.mottak.util

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.kontrakter.ef.felles.StønadType

fun dokumenttypeTilStønadType(dokumenttype: String): StønadType? {
    return when (dokumenttype) {
        DOKUMENTTYPE_OVERGANGSSTØNAD -> StønadType.OVERGANGSSTØNAD
        DOKUMENTTYPE_BARNETILSYN -> StønadType.BARNETILSYN
        DOKUMENTTYPE_SKOLEPENGER -> StønadType.SKOLEPENGER
        else -> null
    }
}
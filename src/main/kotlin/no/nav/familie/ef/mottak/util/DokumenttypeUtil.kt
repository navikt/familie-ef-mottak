package no.nav.familie.ef.mottak.util

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import no.nav.familie.kontrakter.felles.ef.StønadType

fun dokumenttypeTilStønadType(dokumenttype: String): StønadType? =
    when (dokumenttype) {
        DOKUMENTTYPE_OVERGANGSSTØNAD -> StønadType.OVERGANGSSTØNAD
        DOKUMENTTYPE_BARNETILSYN -> StønadType.BARNETILSYN
        DOKUMENTTYPE_SKOLEPENGER -> StønadType.SKOLEPENGER
        else -> null
    }

fun utledDokumenttypeForEttersending(stønadType: StønadType): Dokumenttype =
    when (stønadType) {
        StønadType.OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_ETTERSENDING
        StønadType.SKOLEPENGER -> Dokumenttype.SKOLEPENGER_ETTERSENDING
        StønadType.BARNETILSYN -> Dokumenttype.BARNETILSYNSTØNAD_ETTERSENDING
    }

fun utledDokumenttypeForVedlegg(stønadType: StønadType): Dokumenttype =
    when (stønadType) {
        StønadType.OVERGANGSSTØNAD -> Dokumenttype.OVERGANGSSTØNAD_SØKNAD_VEDLEGG
        StønadType.BARNETILSYN -> Dokumenttype.BARNETILSYNSTØNAD_VEDLEGG
        StønadType.SKOLEPENGER -> Dokumenttype.SKOLEPENGER_VEDLEGG
    }

package no.nav.familie.ef.mottak.hendelse

import io.micrometer.core.instrument.Metrics
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus


fun Journalpost.skalBehandles() = this.erTemaEnfOgTypeI() && this.journalstatus == Journalstatus.MOTTATT && this.gyldigKanal()

fun Journalpost.gyldigKanal() = this.kanal?.substring(0, 5) == "SKAN_" || this.kanal == "NAV_NO"

fun Journalpost.statusIkkeMottattLogString() = "Ignorerer journalhendelse hvor journalpost=${this.journalpostId} har status ${this.journalstatus}"

fun Journalpost.ikkeGyldigKanalLogString() = "Ny journalhendelse med journalpost=${this.journalpostId} med status MOTTATT og kanal ${this.kanal}"

fun Journalpost.kanalMetricName(): String {
    return when {
        this.kanal?.substring(0, 5) == "SKAN_" -> {
            "alene.med.barn.journalhendelse.kanal.skannets"
        }
        this.kanal == "NAV_NO" -> {
            "alene.med.barn.journalhendelse.kanal.navno"
        }
        else -> {
            "alene.med.barn.journalhendelse.kanal.annet"
        }
    }
}

fun Journalpost.incrementMetric() {
    when (this.getJournalpostState()) {
        JournalpostState.IKKE_MOTTATT -> Metrics.counter("alene.med.barn.journalhendelse.ignorerte").increment()
        JournalpostState.UGYLDIG_KANAL,
        JournalpostState.GYLDIG -> Metrics.counter(this.kanalMetricName()).increment()

    }
}

fun Journalpost.getJournalpostState() : JournalpostState {
    return if (!this.erTemaEnfOgTypeI()) {
        JournalpostState.UGYLDIG
    } else if (this.journalstatus != Journalstatus.MOTTATT) {
        JournalpostState.IKKE_MOTTATT
    } else if (!this.gyldigKanal()) {
        JournalpostState.UGYLDIG_KANAL
    } else {
        JournalpostState.GYLDIG
    }
}

enum class JournalpostState {
    UGYLDIG,
    IKKE_MOTTATT,
    UGYLDIG_KANAL,
    GYLDIG;
}
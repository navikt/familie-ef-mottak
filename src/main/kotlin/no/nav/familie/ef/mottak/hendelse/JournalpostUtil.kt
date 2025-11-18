package no.nav.familie.ef.mottak.hendelse

import io.micrometer.core.instrument.Metrics
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.log.IdUtils
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import java.util.Properties

fun Journalpost.skalBehandles() = this.erTemaEnfOgTypeI() && this.journalstatus == Journalstatus.MOTTATT && this.gyldigKanal()

fun Journalpost.gyldigKanal() = this.kanal?.substring(0, 5) == "SKAN_" || this.kanal == "NAV_NO"

fun Journalpost.erTemaEnfOgTypeI() = this.tema == "ENF" && this.journalposttype == Journalposttype.I

fun Journalpost.statusIkkeMottattLogString() = "Ignorerer journalhendelse hvor journalpost=${this.journalpostId} har status ${this.journalstatus}"

fun Journalpost.ikkeGyldigKanalLogString() = "Ny journalhendelse med journalpost=${this.journalpostId} med status MOTTATT og kanal ${this.kanal}"

fun Journalpost.ikkeGyldigLogString() = "Ny journalhendelse ikke gyldig journalpost=${this.journalpostId} tema=${this.tema} journalposttype=${this.journalposttype}"

fun Journalpost.kanalMetricName(): String =
    when {
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

fun Journalpost.incrementMetric() {
    when (this.getJournalpostState()) {
        JournalpostState.IKKE_MOTTATT -> Metrics.counter("alene.med.barn.journalhendelse.ignorerte").increment()

        JournalpostState.UGYLDIG_KANAL, JournalpostState.UGYLDIG,
        JournalpostState.GYLDIG,
        -> Metrics.counter(this.kanalMetricName()).increment()
    }
}

fun Journalpost.metadata(): Properties {
    val journalpost = this
    return Properties().apply {
        this["personIdent"] = journalpost.bruker?.id ?: "Ukjent"
        this["journalpostId"] = journalpost.journalpostId
        if (!MDC.get(MDCConstants.MDC_CALL_ID).isNullOrEmpty()) {
            this["callId"] = MDC.get(MDCConstants.MDC_CALL_ID) ?: IdUtils.generateId()
        }
    }
}

fun Journalpost.getJournalpostState(): JournalpostState =
    if (!this.erTemaEnfOgTypeI()) {
        JournalpostState.UGYLDIG
    } else if (this.journalstatus != Journalstatus.MOTTATT) {
        JournalpostState.IKKE_MOTTATT
    } else if (!this.gyldigKanal()) {
        JournalpostState.UGYLDIG_KANAL
    } else {
        JournalpostState.GYLDIG
    }

enum class JournalpostState {
    UGYLDIG,
    IKKE_MOTTATT,
    UGYLDIG_KANAL,
    GYLDIG,
}

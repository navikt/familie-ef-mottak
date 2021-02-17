package no.nav.familie.ef.mottak.hendelse

import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord


fun JournalfoeringHendelseRecord.erTemaENF() = this.temaNytt?.toString() == "ENF"

fun JournalfoeringHendelseRecord.erHendelsetypeGyldig() =
        arrayOf("MidlertidigJournalført", "TemaEndret").contains(this.hendelsesType.toString())

fun Journalpost.erTemaEnfOgTypeI() =
        this.tema == "ENF" && this.journalposttype == Journalposttype.I
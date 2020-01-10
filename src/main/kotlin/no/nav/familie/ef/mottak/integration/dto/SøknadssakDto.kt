package no.nav.familie.ef.mottak.integration.dto

data class SøknadssakDto(val søknadJson: String,
                         val saksnummer: String,
                         val journalpostId: String)
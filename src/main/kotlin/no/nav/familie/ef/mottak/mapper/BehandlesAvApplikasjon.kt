package no.nav.familie.ef.mottak.mapper

enum class BehandlesAvApplikasjon(val applikasjon: String?, val beskrivelsePrefix: String) {
    EF_SAK("familie-ef-sak", "Må behandles i ny løsning - ")
}

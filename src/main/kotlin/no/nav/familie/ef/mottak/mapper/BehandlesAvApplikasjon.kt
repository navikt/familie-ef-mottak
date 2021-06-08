package no.nav.familie.ef.mottak.mapper

enum class BehandlesAvApplikasjon(val applikasjon: String?, val beskrivelsePrefix: String) {
    EF_SAK("familie-ef-sak", "Må behandles i ny løsning - "),
    EF_SAK_INFOTRYGD("familie-ef-sak-førstegangsbehandling", "Kan behandles i ny løsning - "),
    EF_SAK_BLANKETT("familie-ef-sak-blankett", ""),

    INFOTRYGD(null, ""),
    UAVKLART(null,
             "Søkeren har behandling i ny løsning, må undersøke evt saker i ny løsning før man velger fagsystem. ")
}
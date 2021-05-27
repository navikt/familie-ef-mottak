package no.nav.familie.ef.mottak.mapper

import java.net.URI

enum class BehandlesAvApplikasjon(val applikasjon: String?, val beskrivelsePrefix: (URI) -> String) {
    EF_SAK("familie-ef-sak", { uri -> "Må behandles i ny løsning - $uri - " }),
    EF_SAK_INFOTRYGD("familie-ef-sak-førstegangsbehandling", { uri -> "Kan behandles i ny løsning - $uri - " }),
    EF_SAK_BLANKETT("familie-ef-sak-blankett", { "" }),

    INFOTRYGD(null, { "" }),
    UAVKLART(null,
             { "Søkeren har behandling i ny løsning, må undersøke evt saker i ny løsning før man velger fagsystem. " });
}
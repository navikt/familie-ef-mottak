package no.nav.familie.ef.mottak.mapper

import java.net.URI

enum class BehandlesAvApplikasjon(val navn: String, val beskrivelsePrefix: (URI) -> String) {
    EF_SAK("familie-ef-sak", { uri -> "Må behandles i ny løsning - $uri - " }),
    EF_SAK_INFOTRYGD("familie-ef-sak-førstegangsbehandling", { uri -> "Kan behandles i ny løsning - $uri - " }),
    INFOTRYGD("", { _ -> "" });
}
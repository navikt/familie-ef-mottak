package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.integration.InfotrygdReplikaClient
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.kontrakter.ef.infotrygd.InfotrygdFinnesResponse
import no.nav.familie.kontrakter.ef.infotrygd.InfotrygdSak
import no.nav.familie.kontrakter.ef.infotrygd.InfotrygdSakResponse
import no.nav.familie.kontrakter.ef.infotrygd.InfotrygdSøkRequest
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class InfotrygdService(private val integrasjonerClient: IntegrasjonerClient,
                       private val infotrygdReplikaClient: InfotrygdReplikaClient) {

    /**
     * Finner om en person eksisterer i infotrygd og om det finnes noen aktiv sak for personen.
     */
    fun hentInslagHosInfotrygd(personIdent: String): InfotrygdFinnesResponse {
        val personIdenter = integrasjonerClient.hentIdenter(personIdent, true).map { it.personIdent }.toSet()
        return infotrygdReplikaClient.finnesIInfotrygd(InfotrygdSøkRequest(personIdenter))
    }

    fun hentSaker(personIdent: String): InfotrygdSakResponse {
        val personIdenter = integrasjonerClient.hentIdenter(personIdent, true).map { it.personIdent }.toSet()

        return infotrygdReplikaClient.hentSaker(InfotrygdSøkRequest(personIdenter))
    }

}
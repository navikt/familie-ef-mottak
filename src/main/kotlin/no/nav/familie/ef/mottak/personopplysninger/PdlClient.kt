package no.nav.familie.ef.sak.opplysninger.personopplysninger

import no.nav.familie.ef.mottak.personopplysninger.PdlConfig
import no.nav.familie.ef.mottak.personopplysninger.PdlHentIdenter
import no.nav.familie.ef.mottak.personopplysninger.PdlIdentRequest
import no.nav.familie.ef.mottak.personopplysninger.PdlIdentRequestVariables
import no.nav.familie.ef.mottak.personopplysninger.PdlIdenter
import no.nav.familie.ef.mottak.personopplysninger.PdlResponse
import no.nav.familie.ef.mottak.personopplysninger.feilsjekkOgReturnerData
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.kontrakter.felles.Tema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class PdlClient(
    val pdlConfig: PdlConfig,
    @Qualifier("azureClientCredential") restTemplate: RestOperations,
) : AbstractPingableRestClient(restTemplate, "pdl.personinfo") {
    override val pingUri: URI
        get() = pdlConfig.pdlUri

    override fun ping() {
        operations.optionsForAllow(pingUri)
    }

    /**
     * @param ident Ident til personen, samme hvilke type (Folkeregisterident, aktørid eller npid)
     * @return liste med folkeregisteridenter
     */
    fun hentPersonidenter(ident: String): PdlIdenter {
        val pdlIdentRequest =
            PdlIdentRequest(
                variables = PdlIdentRequestVariables(ident, "FOLKEREGISTERIDENT", historikk = true),
                query = PdlConfig.hentIdentQuery,
            )
        val pdlResponse: PdlResponse<PdlHentIdenter> =
            postForEntity(
                pdlConfig.pdlUri,
                pdlIdentRequest,
                httpHeaders(),
            )
        val pdlIdenter = feilsjekkOgReturnerData(ident, pdlResponse) { it.hentIdenter }

        if (pdlIdenter.identer.isEmpty()) {
            secureLogger.error("Finner ikke personidenter for personIdent i PDL $ident ")
        }
        return pdlIdenter
    }

    /**
     * @param ident Ident til personen, samme hvilke type (Folkeregisterident, aktørid eller npid)
     * @return liste med aktørider
     */
    fun hentAktørIder(ident: String): PdlIdenter {
        val pdlPersonRequest =
            PdlIdentRequest(
                variables = PdlIdentRequestVariables(ident, "AKTORID"),
                query = PdlConfig.hentIdentQuery,
            )
        val pdlResponse: PdlResponse<PdlHentIdenter> =
            postForEntity(
                pdlConfig.pdlUri,
                pdlPersonRequest,
                httpHeaders(),
            )
        return feilsjekkOgReturnerData(ident, pdlResponse) { it.hentIdenter }
    }

    private fun httpHeaders(): HttpHeaders =
        HttpHeaders().apply {
            add("Tema", "ENF")
            add("behandlingsnummer", Tema.ENF.behandlingsnummer)
        }
}

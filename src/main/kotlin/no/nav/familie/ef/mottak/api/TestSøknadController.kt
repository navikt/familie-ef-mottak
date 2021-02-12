package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.FilType
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/testarkiver"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Protected
class TestSøknadController(val integrasjonerClient: IntegrasjonerClient, val soknadRepository: SoknadRepository,
                           val featureToggleService: FeatureToggleService, val pdfClient: PdfClient) {

    @PostMapping
    fun sendInn(): Kvittering {
        if (featureToggleService.isEnabled("familie.ef.mottak.testarkivering")) {
            val lagPdf = pdfClient.lagPdf(mapOf("Hello!" to "World"))
            val arkiverDokumentRequest = ArkiverDokumentRequest("14071850138",
                                                                false,
                                                                hoveddokumentvarianter = listOf(Dokument(lagPdf!!.bytes,
                                                                                                         FilType.PDFA,
                                                                                                         null,
                                                                                                         "hoveddokument",
                                                                                                         DOKUMENTTYPE_OVERGANGSSTØNAD)),
                                                                vedleggsdokumenter = emptyList())
            integrasjonerClient.arkiver(arkiverDokumentRequest)
            return Kvittering("1", "Det gikk bra")
        }
        return Kvittering("", "Ikke tilgjengelig")
    }
}
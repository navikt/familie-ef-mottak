package no.nav.familie.ef.mottak.api

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.Dokument
import no.nav.familie.kontrakter.felles.dokarkiv.FilType
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/testarkiver"], produces = [MediaType.APPLICATION_JSON_VALUE])
@Protected
class TestSøknadController(val integrasjonerClient: IntegrasjonerClient, val soknadRepository: SoknadRepository,
                           val featureToggleService: FeatureToggleService) {

    @PostMapping
    fun sendInn(@RequestBody skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering {
        if (featureToggleService.isEnabled("familie.ef.mottak.testarkivering")) {
            val soknad = soknadRepository.findByJournalpostId("453643988")
            val arkiverDokumentRequest = ArkiverDokumentRequest("07028822477",
                                                                false,
                                                                hoveddokumentvarianter = listOf(Dokument(soknad!!.søknadPdf!!.bytes,
                                                                                                         FilType.PDFA,
                                                                                                         null,
                                                                                                         "hoveddokument",
                                                                                                         soknad!!.dokumenttype)),
                                                                vedleggsdokumenter = emptyList())
            val dokumentResponse = integrasjonerClient.arkiver(arkiverDokumentRequest)
            return Kvittering("1", "Det gikk bra")
        }
        return Kvittering("", "Ikke tilgjengelig")
    }
}
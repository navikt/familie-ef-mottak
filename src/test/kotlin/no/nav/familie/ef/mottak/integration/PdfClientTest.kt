package no.nav.familie.ef.mottak.integration

import io.mockk.mockk
import no.nav.familie.ef.mottak.config.PdfgeneratorConfig
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestOperations


internal class PdfClientTest {

    val pdfgeneratorConfig: PdfgeneratorConfig = PdfgeneratorConfig("https://familie-ef-dokgen.dev-adeo.no/")
    private val operations: RestOperations = mockk()
    private val pdfClient: PdfClient = PdfClient(operations, pdfgeneratorConfig)

    @Test
    fun `Skal arkivere  søknad`() {

//        val pdf = pdfClient.lagPdf(labelValueJson);
//        assertNotNull(pdf)
    }


}



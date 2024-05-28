package no.nav.familie.ef.mottak.mockapi

import io.mockk.mockk
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.net.URI

@Configuration
class MockConfiguration {
    @Bean
    @Primary
    @Profile("mock-pdf")
    fun pdfClient(): PdfClient =
        object : PdfClient(mockk(), mockk()) {
            override fun lagPdf(labelValueJson: Map<String, Any>): ByteArray {
                val pdf = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(labelValueJson)
                log.info("Creating pdf: $pdf")
                return labelValueJson.toString().toByteArray()
            }
        }

    @Bean
    @Primary
    @Profile("mock-integrasjon")
    fun integrasjonerClient(): IntegrasjonerClient =
        object : IntegrasjonerClient(
            mockk(),
            IntegrasjonerConfig(URI.create("http://bac")),
        ) {
            override fun arkiver(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse {
                return ArkiverDokumentResponse("journalpostId1", true)
            }

            override fun lagOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OppgaveResponse {
                return OppgaveResponse(1)
            }

            override fun hentSaksnummer(journalPostId: String): String {
                return "sak1"
            }

            override fun hentAktørId(personident: String): String {
                return "aktørId"
            }
        }
}

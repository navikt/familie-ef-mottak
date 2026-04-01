package no.nav.familie.ef.mottak.mockapi

import io.mockk.mockk
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.ef.mottak.integration.FamilieBrevClient
import no.nav.familie.ef.mottak.integration.FamiliePdfClient
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.FeltMap
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.dokarkiv.v2.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.jsonMapper
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
    fun familieBrevClient(): FamilieBrevClient =
        object : FamilieBrevClient(mockk(), mockk()) {
            override fun lagPdf(labelValueJson: FeltMap): ByteArray {
                val pdf = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(labelValueJson)
                log.info("Creating pdf: $pdf")
                return labelValueJson.toString().toByteArray()
            }
        }

    @Bean
    @Primary
    @Profile("mock-pdf-soknad")
    fun familiePdfClient(): FamiliePdfClient =
        object : FamiliePdfClient(URI.create("uri"), mockk()) {
            override fun lagPdf(labelValueJson: FeltMap): ByteArray {
                val pdf = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(labelValueJson)
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
            override fun arkiver(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse = ArkiverDokumentResponse("journalpostId1", true)

            override fun lagOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OppgaveResponse = OppgaveResponse(1)

            override fun hentSaksnummer(journalPostId: String): String = "sak1"
        }
}

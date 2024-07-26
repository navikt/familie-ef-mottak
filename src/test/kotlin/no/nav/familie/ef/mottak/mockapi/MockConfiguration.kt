package no.nav.familie.ef.mottak.mockapi

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.integration.PdfClient
import no.nav.familie.ef.mottak.personopplysninger.PdlIdent
import no.nav.familie.ef.mottak.personopplysninger.PdlIdenter
import no.nav.familie.ef.mottak.personopplysninger.PdlNotFoundException
import no.nav.familie.ef.sak.opplysninger.personopplysninger.PdlClient
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
            override fun arkiver(arkiverDokumentRequest: ArkiverDokumentRequest): ArkiverDokumentResponse = ArkiverDokumentResponse("journalpostId1", true)

            override fun lagOppgave(opprettOppgaveRequest: OpprettOppgaveRequest): OppgaveResponse = OppgaveResponse(1)

            override fun hentSaksnummer(journalPostId: String): String = "sak1"
        }

    @Bean
    @Primary
    @Profile("mock-pdl")
    fun pdlClient(): PdlClient {
        val pdlClient: PdlClient = mockk()

        every { pdlClient.ping() } just runs

        val personIdentAktør = slot<String>()
        every { pdlClient.hentAktørIder(capture(personIdentAktør)) } answers {
            if (personIdentAktør.captured == "19117313797") {
                throw PdlNotFoundException()
            } else {
                PdlIdenter(listOf(PdlIdent("12345678901232", false)))
            }
        }

        val personIdent = slot<String>()
        every { pdlClient.hentPersonidenter(capture(personIdent)) } answers {
            if (personIdent.captured == "19117313797") {
                throw PdlNotFoundException()
            } else {
                PdlIdenter(listOf(PdlIdent(firstArg(), false), PdlIdent("98765432109", true)))
            }
        }

        return pdlClient
    }
}

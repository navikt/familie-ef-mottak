package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.integration

import no.nav.familie.ef.mottak.ApplicationLocal
import no.nav.familie.ef.mottak.integration.ArkivClient
import no.nav.familie.ef.mottak.mapper.ArkiverDokumentRequestMapper
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ActiveProfiles("local")
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = [ApplicationLocal::class])
class ArkivklientIntegrationTest {

    @Autowired
    lateinit var arkivClient: ArkivClient

    @Test
    fun `Skal motta saksnummer fra Klient`() {
        val saksnummer = arkivClient.hentSaksnummer("hvaSomHelst")
        assertNotNull(saksnummer)
    }

    @Test
    fun `Skal arkivere og motta journalpostId fra klient`() {
        val søknad =
                Soknad(UUID.randomUUID().toString(), "hvaSomHelst", Fil(ByteArray(5) { 2 }), "hvaSomHelst", null, "hvaSomHelst")
        val arkiverDokumentRequest = ArkiverDokumentRequestMapper.toDto(søknad)
        val arkivDokumentRespons = arkivClient.arkiver(arkiverDokumentRequest)
        assertNotNull(arkivDokumentRespons)
        assertTrue(arkivDokumentRespons.ferdigstilt)
    }


}


package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class DbMigreringServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var soknadRepository: SoknadRepository

    lateinit var dbMigreringService: DbMigreringService

    @BeforeEach
    fun setUp() {
        dbMigreringService = DbMigreringService(soknadRepository)
    }

    @Test
    fun `dbMigrering oppdaterer soknad med korrekt verdi`() {
        val gammeltSaksnummerFormat = this::class.java.getResource("/json/saksnummer.json").readText()
        val lagretSøknad =
                soknadRepository.save(Soknad(dokumenttype = "", saksnummer = gammeltSaksnummerFormat, søknadJson = "", fnr = ""))

        dbMigreringService.dbMigrering()

        val oppdatertSoknad = soknadRepository.getOne(lagretSøknad.id)
        assertThat(oppdatertSoknad.saksnummer).isEqualTo("140258871")
    }
}
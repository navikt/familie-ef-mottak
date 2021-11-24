package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.repository.DbMigreringRepository
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
internal class DbMigreringServiceTest : IntegrasjonSpringRunnerTest() {

    @Autowired
    lateinit var dbMigreringRepository: DbMigreringRepository

    @Autowired
    lateinit var taskRepository: TaskRepository

    lateinit var dbMigreringService: DbMigreringService

    @BeforeEach
    fun setUp() {
        dbMigreringService = DbMigreringService(dbMigreringRepository)
    }

}

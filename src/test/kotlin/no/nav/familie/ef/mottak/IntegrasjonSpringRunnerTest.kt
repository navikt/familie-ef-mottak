package no.nav.familie.ef.mottak

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.util.DbContainerInitializer
import no.nav.familie.prosessering.internal.TaskService
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [DbContainerInitializer::class])
@SpringBootTest(classes = [ApplicationLocalConfig::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(
    "integrationtest",
    "mock-integrasjon",
    "mock-dokument",
    "mock-ef-sak",
    "mock-pdf",
    "mock-pdl",
)
@EnableMockOAuth2Server
abstract class IntegrasjonSpringRunnerTest {
    protected val listAppender = initLoggingEventListAppender()
    protected var loggingEvents: MutableList<ILoggingEvent> = listAppender.list
    protected val restTemplate = TestRestTemplate()
    protected val headers = HttpHeaders()

    @Autowired
    private lateinit var søknadRepository: SøknadRepository

    @Autowired
    private lateinit var vedleggRepository: VedleggRepository

    @Autowired
    private lateinit var dokumentasjonsbehovRepository: DokumentasjonsbehovRepository

    @Autowired
    private lateinit var ettersendingRepository: EttersendingRepository

    @Autowired
    private lateinit var ettersendingVedleggRepository: EttersendingVedleggRepository

    @Autowired
    private lateinit var taskService: TaskService

    @Autowired
    private lateinit var hendelsesloggRepository: HendelsesloggRepository

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @LocalServerPort
    private var port: Int? = 0

    @AfterEach
    fun reset() {
        loggingEvents.clear()
        dokumentasjonsbehovRepository.deleteAll()
        vedleggRepository.deleteAll()
        søknadRepository.deleteAll()
        taskService.deleteAll(taskService.findAll().toList())
        ettersendingVedleggRepository.deleteAll()
        ettersendingRepository.deleteAll()
        hendelsesloggRepository.deleteAll()
    }

    protected fun getPort(): String = port.toString()

    protected fun localhost(uri: String): String = LOCALHOST + getPort() + uri

    protected fun url(
        baseUrl: String,
        uri: String,
    ): String = baseUrl + uri

    fun søkerBearerToken(personident: String = "12345678910"): String {
        val clientId = "lokal:teamfamilie:familie-ef-mottak"
        return mockOAuth2Server
            .issueToken(
                issuerId = "tokenx",
                clientId,
                DefaultOAuth2TokenCallback(
                    issuerId = "tokenx",
                    subject = personident,
                    audience = listOf("aud-localhost"),
                    claims = mapOf("acr" to "Level4", "client_id" to clientId),
                    expiry = 3600,
                ),
            ).serialize()
    }

    companion object {
        private const val LOCALHOST = "http://localhost:"

        protected fun initLoggingEventListAppender(): ListAppender<ILoggingEvent> {
            val listAppender = ListAppender<ILoggingEvent>()
            listAppender.start()
            return listAppender
        }
    }
}

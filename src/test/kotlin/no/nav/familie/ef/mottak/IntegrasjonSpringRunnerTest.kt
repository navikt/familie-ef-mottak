package no.nav.familie.ef.mottak

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.HendelsesloggRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.prosessering.domene.TaskRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [ApplicationLocal::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:mottakdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE;MODE=POSTGRESQL",
    "spring.datasrouce.username=sa",
    "spring.datasrouce.password=",
    "spring.datasrouce.driver-class-name=org.h2.Driver"
])
abstract class IntegrasjonSpringRunnerTest {

    protected val listAppender = initLoggingEventListAppender()
    protected var loggingEvents: MutableList<ILoggingEvent> = listAppender.list
    protected val restTemplate = TestRestTemplate()
    protected val headers = HttpHeaders()

    @Autowired private lateinit var søknadRepository: SøknadRepository
    @Autowired private lateinit var vedleggRepository: VedleggRepository
    @Autowired private lateinit var dokumentasjonsbehovRepository: DokumentasjonsbehovRepository
    @Autowired private lateinit var ettersendingRepository: EttersendingRepository
    @Autowired private lateinit var ettersendingVedleggRepository: EttersendingVedleggRepository
    @Autowired private lateinit var taskRepository: TaskRepository
    @Autowired private lateinit var hendelsesloggRepository: HendelsesloggRepository

    @LocalServerPort
    private var port: Int? = 0

    @AfterEach
    fun reset() {
        loggingEvents.clear()
        dokumentasjonsbehovRepository.deleteAllInBatch()
        vedleggRepository.deleteAllInBatch()
        søknadRepository.deleteAllInBatch()
        taskRepository.deleteAll()
        ettersendingVedleggRepository.deleteAll()
        ettersendingRepository.deleteAll()
        hendelsesloggRepository.deleteAll()
    }

    protected fun getPort(): String {
        return port.toString()
    }

    protected fun localhost(uri: String): String {
        return LOCALHOST + getPort() + uri
    }

    protected fun url(baseUrl: String, uri: String): String {
        return baseUrl + uri
    }

    protected val lokalTestToken: String
        get() {
            return getTestToken()
        }

    fun getTestToken(fnr: String = "12345678910"): String {
        val cookie = restTemplate.exchange(localhost("/local/cookie?subject=$fnr"),
                                           HttpMethod.GET,
                                           HttpEntity.EMPTY,
                                           String::class.java)
        return tokenFraRespons(cookie)
    }


    private fun tokenFraRespons(cookie: ResponseEntity<String>): String {
        return cookie.body!!.split("value\":\"".toRegex()).toTypedArray()[1].split("\"".toRegex()).toTypedArray()[0]
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

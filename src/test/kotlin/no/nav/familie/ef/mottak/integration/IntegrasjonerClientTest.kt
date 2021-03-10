package no.nav.familie.ef.mottak.integration

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.mockk.every
import io.mockk.mockk
import no.nav.familie.ef.mottak.config.IntegrasjonerConfig
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentRequest
import no.nav.familie.kontrakter.felles.dokarkiv.ArkiverDokumentResponse
import no.nav.familie.kontrakter.felles.infotrygdsak.InfotrygdSak
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakRequest
import no.nav.familie.kontrakter.felles.infotrygdsak.OpprettInfotrygdSakResponse
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestOperations
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

internal class IntegrasjonerClientTest {

    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val restOperations: RestOperations = RestTemplateBuilder().build()

    private lateinit var integrasjonerClient: IntegrasjonerClient
    private val arkiverSøknadRequest = ArkiverDokumentRequest("123456789", true, listOf())
    private val arkiverDokumentResponse: ArkiverDokumentResponse = ArkiverDokumentResponse("wer", true)

    @BeforeEach
    fun setUp() {
        wireMockServer.start()
        val stsRestClient = mockk<StsRestClient>()
        every { stsRestClient.systemOIDCToken } returns "token"
        integrasjonerClient = IntegrasjonerClient(restOperations, IntegrasjonerConfig(URI.create(wireMockServer.baseUrl())))
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.resetAll()
        wireMockServer.stop()
    }

    @Test
    fun `skal kunne plukke ut hele feilmeldingen fra ressurs selv om body er større enn 400`() {
        val feilmelding = """Feilmelding mottatt i svar fra Aktoerregisteret - Den angitte personidenten finnes ikke"""
        val feil = """{
                            "data": null,
                            "status": "FEILET",
                            "melding": "[Oppgave.opprettOppgave][Feil ved oppretting av oppgave for 2097622931607. Response fra oppgave = {\"uuid\":\"5a1d60be-7a68-4362-8cc7-5af4014ec174\",\"feilmelding\":\"$feilmelding\"}][org.springframework.web.client.HttpClientErrorException$ BadRequest]",
                            "frontendFeilmelding": null,
                            "stacktrace": "no.nav.familie.integrasjoner.felles.OppslagException: Feil ved oppretting av oppgave for 2097622931607. Response fra oppgave = {\"uuid\":\"5a1d60be-7a68-4362-8cc7-5af4014ec174\",\"feilmelding\":\"Feilmelding mottatt i svar fra Aktoerregisteret - Den angitte personidenten finnes ikke\"}\n\tat no.nav.familie.integrasjoner.client.rest.OppgaveRestClient.opprettOppgave(OppgaveRestClient.kt:153)\n\tat no.nav.familie.integrasjoner.oppgave.OppgaveService.opprettOppgave(OppgaveService.kt:131)\n\tat no.nav.familie.integrasjoner.oppgave.OppgaveService${'$'}$ FastClassBySpringCGLIB${'$'}$ d0aafd93.invoke(<generated>)\n\tat org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)\n\tat org.springframework.aop.framework.CglibAopProxy$ CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:779)\n\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)\n\tat org.springframework.aop.framework.CglibAopProxy$ CglibMethodInvocation.proceed(CglibAopProxy.java:750)\n\tat org.springframework.aop.support.DelegatingIntroductionInterceptor.doProceed(DelegatingIntroductionInterceptor.java:137)\n\tat org.springframework.aop.support.DelegatingIntroductionInterceptor.invoke(DelegatingIntroductionInterceptor.java:124)\n\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)\n\tat org.springframework.aop.framework.CglibAopProxy$ CglibMethodInvocation.proceed(CglibAopProxy.java:750)\n\tat org.springframework.aop.framework.CglibAopProxy$ DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:692)\n\tat no.nav.familie.integrasjoner.oppgave.OppgaveService${'$'}$ EnhancerBySpringCGLIB${'$'}${'$'}19dd306f.opprettOppgave(<generated>)\n\tat no.nav.familie.integrasjoner.oppgave.OppgaveController.opprettOppgaveV2(OppgaveController.kt:78)\n\tat jdk.internal.reflect.GeneratedMethodAccessor310.invoke(Unknown Source)\n\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n\tat java.base/java.lang.reflect.Method.invoke(Method.java:566)\n\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:197)\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:141)\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106)\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:894)\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808)\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1060)\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:962)\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)\n\tat org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:517)\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)\n\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:584)\n\tat org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:791)\n\tat org.eclipse.jetty.servlet.ServletHandler$ ChainEnd.doFilter(ServletHandler.java:1626)\n\tat org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter.doFilter(WebSocketUpgradeFilter.java:228)\n\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\n\tat org.eclipse.jetty.servlet.ServletHandler$ Chain.doFilter(ServletHandler.java:1601)\n\tat no.nav.familie.log.filter.LogFilter.filterWithErrorHandling(LogFilter.kt:67)\n\tat no.nav.familie.log.filter.LogFilter.doFilter(LogFilter.kt:53)\n\tat javax.servlet.http.HttpFilter.doFilter(HttpFilter.java:97)\n\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\n\tat org.eclipse.jetty.servlet.ServletHandler$ Chain.doFilter(ServletHandler.java:1601)\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\n\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\n\tat org.eclipse.jetty.servlet.ServletHandler$ Chain.doFilter(ServletHandler.java:1601)\n\tat org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:93)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\n\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\n\tat org.eclipse.jetty.servlet.ServletHandler$ Chain.doFilter(ServletHandler.java:1601)\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\n\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\n\tat org.eclipse.jetty.servlet.ServletHandler$ Chain.doFilter(ServletHandler.java:1601)\n\tat no.nav.security.token.support.filter.JwtTokenValidationFilter.doTokenValidation(JwtTokenValidationFilter.java:51)\n\tat no.nav.security.token.support.filter.JwtTokenValidationFilter.doFilter(JwtTokenValidationFilter.java:35)\n\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\n\tat org.eclipse.jetty.servlet.ServletHandler$ Chain.doFilter(ServletHandler.java:1601)\n\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:548)\n\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\n\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:602)\n\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:127)\n\tat org.eclipse.jetty.server.handler.ScopedHandler.nextHandle(ScopedHandler.java:235)\n\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:1624)\n\tat org.eclipse.jetty.server.handler.ScopedHandler.nextHandle(ScopedHandler.java:233)\n\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1435)\n\tat org.eclipse.jetty.server.handler.ScopedHandler.nextScope(ScopedHandler.java:188)\n\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:501)\n\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:1594)\n\tat org.eclipse.jetty.server.handler.ScopedHandler.nextScope(ScopedHandler.java:186)\n\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1350)\n\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\n\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:127)\n\tat org.eclipse.jetty.server.Server.handle(Server.java:516)\n\tat org.eclipse.jetty.server.HttpChannel.lambda$ handle${'$'}1(HttpChannel.java:388)\n\tat org.eclipse.jetty.server.HttpChannel.dispatch(HttpChannel.java:633)\n\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:380)\n\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:273)\n\tat org.eclipse.jetty.io.AbstractConnection$ ReadCallback.succeeded(AbstractConnection.java:311)\n\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:105)\n\tat org.eclipse.jetty.io.ChannelEndPoint${'$'}1.run(ChannelEndPoint.java:104)\n\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.runTask(EatWhatYouKill.java:336)\n\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.doProduce(EatWhatYouKill.java:313)\n\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.tryProduce(EatWhatYouKill.java:171)\n\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.run(EatWhatYouKill.java:129)\n\tat org.eclipse.jetty.util.thread.ReservedThreadExecutor$ ReservedThread.run(ReservedThreadExecutor.java:375)\n\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:773)\n\tat org.eclipse.jetty.util.thread.QueuedThreadPool$ Runner.run(QueuedThreadPool.java:905)\n\tat java.base/java.lang.Thread.run(Thread.java:834)\n"
                        }""";
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_OPPRETT_OPPGAVE}"))
                .willReturn(serverError().withBody(feil)))
        try {
            integrasjonerClient.lagOppgave(OpprettOppgaveRequest(ident = OppgaveIdentV2("asd", IdentGruppe.AKTOERID),
                    saksId = null,
                    journalpostId = "123",
                    tema = Tema.ENF,
                    oppgavetype = Oppgavetype.Journalføring,
                    fristFerdigstillelse = LocalDate.now(),
                    beskrivelse = "",
                    behandlingstema = "sad",
                    enhetsnummer = null))
        } catch (e: HttpStatusCodeException) {
            val response: Ressurs<OppgaveResponse> = objectMapper.readValue(e.getResponseBodyAsString())
            assertThat(response.melding).contains(feilmelding)
            assertThat(e.message).doesNotContain(feilmelding)
        }

    }

    @Test
    fun `opprettInfotrygdsak returnerer OpprettInfotrygdsakResponse`() {
        val opprettInfotrygdSakRequest = OpprettInfotrygdSakRequest(fagomrade = "ENF",
                                                                    fnr = "fnr",
                                                                    mottakerOrganisasjonsEnhetsId = "4408",
                                                                    mottattdato = LocalDate.of(2010, 11, 12),
                                                                    oppgaveId = "oppgaveId",
                                                                    oppgaveOrganisasjonsenhetId = "4408",
                                                                    opprettetAvOrganisasjonsEnhetsId = "4408",
                                                                    sendBekreftelsesbrev = false,
                                                                    stonadsklassifisering2 = "OG",
                                                                    type = "K")
        val opprettInfotrygdSakResponse = OpprettInfotrygdSakResponse(saksId = "OG65")
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_INFOTRYGDSAK}/opprett"))
                                       .willReturn(okJson(objectMapper.writeValueAsString(success(opprettInfotrygdSakResponse)))))

        val testresultat = integrasjonerClient.opprettInfotrygdsak(opprettInfotrygdSakRequest)

        assertThat(testresultat).isEqualToComparingFieldByField(opprettInfotrygdSakResponse)
    }

    @Test
    fun `ferdigstillJournalpost sender melding om ferdigstilling parser payload og returnerer saksnummer`() {
        val journalpostId = "321"
        val journalførendeEnhet = "9999"
        val json = objectMapper.writeValueAsString(success(mapOf("journalpostId" to journalpostId),
                                                           "Ferdigstilt journalpost $journalpostId"))
        wireMockServer.stubFor(put(urlEqualTo("/arkiv/v2/$journalpostId/ferdigstill?journalfoerendeEnhet=$journalførendeEnhet"))
                                       .willReturn(okJson(json)))

        val testresultat = integrasjonerClient.ferdigstillJournalpost(journalpostId, journalførendeEnhet)

        assertThat(testresultat["journalpostId"]).isEqualTo(journalpostId)
    }

    @Test
    fun `hentSaksnummer parser payload og returnerer saksnummer`() {
        wireMockServer.stubFor(get(urlEqualTo("/${IntegrasjonerClient.PATH_HENT_SAKSNUMMER}?journalpostId=123"))
                                       .willReturn(okJson(readFile("saksnummer.json"))))

        assertThat(integrasjonerClient.hentSaksnummer("123")).isEqualTo("140258871")
    }

    @Test
    fun `Skal arkivere søknad`() {
        // Gitt
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_SEND_INN}"))
                                       .willReturn(okJson(success(arkiverDokumentResponse).toJson())))
        // Vil gi resultat
        assertNotNull(integrasjonerClient.arkiver(arkiverSøknadRequest))
    }

    @Test
    fun `Skal ikke arkivere søknad`() {
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_SEND_INN}"))
                                       .willReturn(okJson(failure<Any>("error").toJson())))

        assertFailsWith(IllegalStateException::class) {
            integrasjonerClient.arkiver(arkiverSøknadRequest)
        }
    }
    @Test
    fun `Skal finne infotrygdsaksnummer`() {
        // Gitt
        val fnr = "12345678901"
        val registrertNavEnhetId = "0304"
        val fagomrade = "ENF"
        val infotrygdSaker = listOf(
                InfotrygdSak(fnr, "A01", registrertNavEnhetId, fagomrade),
                InfotrygdSak(fnr, "A03", registrertNavEnhetId, fagomrade),
                InfotrygdSak(fnr, "A02", registrertNavEnhetId, fagomrade)
        )
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_INFOTRYGDSAK}/soek"))
                                       .willReturn(okJson(success(infotrygdSaker).toJson())))
        // Vil gi resultat
        assertThat(integrasjonerClient.finnInfotrygdSaksnummerForSak("A01", fagomrade, fnr)).isEqualTo("0304A01")
        assertThat(integrasjonerClient.finnInfotrygdSaksnummerForSak("A02", fagomrade, fnr)).isEqualTo("0304A02")
        assertThat(integrasjonerClient.finnInfotrygdSaksnummerForSak("A03", fagomrade, fnr)).isEqualTo("0304A03")
        assertThrows<IllegalStateException> {
            integrasjonerClient.finnInfotrygdSaksnummerForSak("A04", fagomrade, fnr)
        }
    }

    @Test
    fun `Skal finne infotrygdsaksnummer med jsondata og unødige whitespaces`() {
        // Gitt
        val fnr = "04087420901"
        val fagomrade = "ENF"
        val infotrygdData = """
            {
                "data": [
                    {
                        "fnr": "04087420901",
                        "saksnr": "A01       ",
                        "registrertNavEnhetId": "0314",
                        "fagomrade": "EF"
                    }
                ],
                "status": "SUKSESS",
                "melding": "Innhenting av data var vellykket",
                "frontendFeilmelding": null,
                "stacktrace": null
            }
        """.trimIndent()
        wireMockServer.stubFor(post(urlEqualTo("/${IntegrasjonerClient.PATH_INFOTRYGDSAK}/soek"))
                                       .willReturn(okJson(infotrygdData)))
        // Vil gi resultat
        assertThat(integrasjonerClient.finnInfotrygdSaksnummerForSak("A01", fagomrade, fnr)).isEqualTo("0314A01")
        assertThrows<IllegalStateException> {
            integrasjonerClient.finnInfotrygdSaksnummerForSak("A04", fagomrade, fnr)
        }
    }

    private fun readFile(filnavn: String): String {
        return this::class.java.getResource("/json/$filnavn").readText()
    }
}




package no.nav.familie.ef.mottak.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.mapper.OpprettOppgaveMapper
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.sak.DokumentBrevkode
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.journalpost.*
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import java.nio.charset.Charset
import kotlin.test.assertEquals

internal class OppgaveServiceTest {

    private val integrasjonerClient: IntegrasjonerClient = mockk()
    private val søknadService: SøknadService = mockk()
    private val opprettOppgaveMapper = OpprettOppgaveMapper(integrasjonerClient)
    private val oppgaveService: OppgaveService = OppgaveService(integrasjonerClient, søknadService, opprettOppgaveMapper)


    @BeforeEach
    private fun init() {
        every {
            integrasjonerClient.hentAktørId(any())
        } returns Testdata.randomAktørId()
    }


    @Test
    fun `Skal kalle integrasjonsklient ved opprettelse av oppgave`() {
        every {
            integrasjonerClient.lagOppgave(any())
        } returns OppgaveResponse(oppgaveId = 1)
        every { integrasjonerClient.hentJournalpost("999") }
                .returns(Journalpost("999",
                                     Journalposttype.I,
                                     Journalstatus.MOTTATT,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     null,
                                     listOf(DokumentInfo("1", "", "", null, null, null)),
                                     null))
        every { integrasjonerClient.finnOppgaver(any(), any()) } returns FinnOppgaveResponseDto(0L, emptyList())
        every {
            søknadService.get("123")
        } returns Soknad(søknadJson = "{}",
                         dokumenttype = DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER,
                         journalpostId = "999",
                         fnr = Testdata.randomFnr())


        oppgaveService.lagJournalføringsoppgaveForSøknadId("123")

        verify(exactly = 1) {
            integrasjonerClient.lagOppgave(any())
        }
    }

    @Test
    fun `Opprett oppgave med enhet NAY hvis opprettOppgave-kall får feil som følge av at enhet ikke blir funnet for bruker`() {

        val opprettOppgaveRequest = opprettOppgaveMapper.toDto(journalpost)
        every {
            integrasjonerClient.lagOppgave(opprettOppgaveRequest)
        } throws HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,  "Server error", feilmeldingFraOppgave.toJson().toByteArray(), Charset.defaultCharset() )

        val forventetOpprettOppgaveRequestMedNayEnhet = opprettOppgaveRequest.copy(enhetsnummer = "4489")
        every {
            integrasjonerClient.lagOppgave(forventetOpprettOppgaveRequestMedNayEnhet)
        } answers {
            OppgaveResponse(1)
        }

        every {
            integrasjonerClient.finnOppgaver(any(), any())
        } returns FinnOppgaveResponseDto(0, listOf())

        val oppgaveResponse = oppgaveService.lagJournalføringsoppgave(journalpost)


        assertEquals(1, oppgaveResponse)
    }

    private val journalpost =
        Journalpost(
            journalpostId = "111111111",
            journalposttype = Journalposttype.I,
            journalstatus = Journalstatus.MOTTATT,
            tema = "ENF",
            behandlingstema = "ab0071",
            tittel = "abrakadabra",
            bruker = Bruker(type = BrukerIdType.AKTOERID, id = "3333333333333"),
            journalforendeEnhet = "4817",
            kanal = "SKAN_IM",
            sak = Sak(null, null, null),
            dokumenter =
            listOf(
                DokumentInfo(
                    dokumentInfoId = "12345",
                    tittel = "Tittel",
                    brevkode = DokumentBrevkode.OVERGANGSSTØNAD.verdi,
                    dokumentvarianter = listOf(Dokumentvariant(variantformat = "ARKIV"))
                )
            )
        )


    val feilmeldingFraOppgave: Ressurs<OppgaveResponse> = Ressurs.failure(
            errorMessage = "[Oppgave.opprettOppgave][Feil ved oppretting av oppgave for 1111111111111. Response fra oppgave = {\"uuid\":\"1uuid1-1234-4321-uuid-1234\",\"feilmelding\":\"Fant ingen gyldig arbeidsfordeling for oppgaven\"}\\\"}][org.springframework.web.client.HttpClientErrorException\$ BadRequest]",
            error = IllegalArgumentException("no.nav.familie.integrasjoner.felles.OppslagException: Feil ved oppretting av oppgave for 1111111111111. Response fra oppgave = {\"uuid\":\"1uuid1-1234-4321-uuid-1234\",\"feilmelding\":\"Fant ingen gyldig arbeidsfordeling for oppgaven\"}\\\"}\\n\\tat no.nav.familie.integrasjoner.client.rest.OppgaveRestClient.opprettOppgave(OppgaveRestClient.kt:153)\\n\\tat no.nav.familie.integrasjoner.oppgave.OppgaveService.opprettOppgave(OppgaveService.kt:131)\\n\\tat no.nav.familie.integrasjoner.oppgave.OppgaveService\$\$ FastClassBySpringCGLIB\$\$ d0aafd93.invoke(<generated>)\\n\\tat org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)\\n\\tat org.springframework.aop.framework.CglibAopProxy\$ CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:779)\\n\\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)\\n\\tat org.springframework.aop.framework.CglibAopProxy\$ CglibMethodInvocation.proceed(CglibAopProxy.java:750)\\n\\tat org.springframework.aop.support.DelegatingIntroductionInterceptor.doProceed(DelegatingIntroductionInterceptor.java:137)\\n\\tat org.springframework.aop.support.DelegatingIntroductionInterceptor.invoke(DelegatingIntroductionInterceptor.java:124)\\n\\tat org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)\\n\\tat org.springframework.aop.framework.CglibAopProxy\$ CglibMethodInvocation.proceed(CglibAopProxy.java:750)\\n\\tat org.springframework.aop.framework.CglibAopProxy\$ DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:692)\\n\\tat no.nav.familie.integrasjoner.oppgave.OppgaveService\$\$ EnhancerBySpringCGLIB\$\$19dd306f.opprettOppgave(<generated>)\\n\\tat no.nav.familie.integrasjoner.oppgave.OppgaveController.opprettOppgaveV2(OppgaveController.kt:78)\\n\\tat jdk.internal.reflect.GeneratedMethodAccessor310.invoke(Unknown Source)\\n\\tat java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\\n\\tat java.base/java.lang.reflect.Method.invoke(Method.java:566)\\n\\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:197)\\n\\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:141)\\n\\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:106)\\n\\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:894)\\n\\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:808)\\n\\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87)\\n\\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1060)\\n\\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:962)\\n\\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1006)\\n\\tat org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:909)\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:517)\\n\\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:883)\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:584)\\n\\tat org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:791)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ ChainEnd.doFilter(ServletHandler.java:1626)\\n\\tat org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter.doFilter(WebSocketUpgradeFilter.java:228)\\n\\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ Chain.doFilter(ServletHandler.java:1601)\\n\\tat no.nav.familie.log.filter.LogFilter.filterWithErrorHandling(LogFilter.kt:67)\\n\\tat no.nav.familie.log.filter.LogFilter.doFilter(LogFilter.kt:53)\\n\\tat javax.servlet.http.HttpFilter.doFilter(HttpFilter.java:97)\\n\\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ Chain.doFilter(ServletHandler.java:1601)\\n\\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\\n\\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\\n\\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ Chain.doFilter(ServletHandler.java:1601)\\n\\tat org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:93)\\n\\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\\n\\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ Chain.doFilter(ServletHandler.java:1601)\\n\\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:201)\\n\\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:119)\\n\\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ Chain.doFilter(ServletHandler.java:1601)\\n\\tat no.nav.security.token.support.filter.JwtTokenValidationFilter.doTokenValidation(JwtTokenValidationFilter.java:51)\\n\\tat no.nav.security.token.support.filter.JwtTokenValidationFilter.doFilter(JwtTokenValidationFilter.java:35)\\n\\tat org.eclipse.jetty.servlet.FilterHolder.doFilter(FilterHolder.java:193)\\n\\tat org.eclipse.jetty.servlet.ServletHandler\$ Chain.doFilter(ServletHandler.java:1601)\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:548)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:143)\\n\\tat org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:602)\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:127)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.nextHandle(ScopedHandler.java:235)\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:1624)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.nextHandle(ScopedHandler.java:233)\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1435)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.nextScope(ScopedHandler.java:188)\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:501)\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:1594)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.nextScope(ScopedHandler.java:186)\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1350)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:127)\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:516)\\n\\tat org.eclipse.jetty.server.HttpChannel.lambda\$ handle\$1(HttpChannel.java:388)\\n\\tat org.eclipse.jetty.server.HttpChannel.dispatch(HttpChannel.java:633)\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:380)\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:273)\\n\\tat org.eclipse.jetty.io.AbstractConnection\$ ReadCallback.succeeded(AbstractConnection.java:311)\\n\\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:105)\\n\\tat org.eclipse.jetty.io.ChannelEndPoint\$1.run(ChannelEndPoint.java:104)\\n\\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.runTask(EatWhatYouKill.java:336)\\n\\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.doProduce(EatWhatYouKill.java:313)\\n\\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.tryProduce(EatWhatYouKill.java:171)\\n\\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.run(EatWhatYouKill.java:129)\\n\\tat org.eclipse.jetty.util.thread.ReservedThreadExecutor\$ ReservedThread.run(ReservedThreadExecutor.java:375)\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:773)\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool\$ Runner.run(QueuedThreadPool.java:905)\\n\\tat java.base/java.lang.Thread.run(Thread.java:834)\\n")
    )



}
package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.oppgave.IdentGruppe
import no.nav.familie.kontrakter.felles.oppgave.OppgaveIdentV2
import no.nav.familie.kontrakter.felles.oppgave.Oppgavetype
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class OpprettOppgaveMapper(private val integrasjonerClient: IntegrasjonerClient) {

    val logger = LoggerFactory.getLogger(javaClass)

    /**
     * En liten "hack", kanskje midlertidig.
     * - Kode for "klage", som brukes for å evt sette behandlingstype tilsvarende "klage"
     */
    val KODEVERK_KLAGE = "ae0058"

    fun toJournalføringsoppgave(journalpost: Journalpost,
                                behandlesAvApplikasjon: BehandlesAvApplikasjon,
                                tilordnet: String? = null) =
            OpprettOppgaveRequest(ident = tilOppgaveIdent(journalpost),
                                  saksId = null,
                                  journalpostId = journalpost.journalpostId,
                                  tema = Tema.ENF,
                                  oppgavetype = Oppgavetype.Journalføring,
                                  fristFerdigstillelse = lagFristForOppgave(LocalDateTime.now()),
                                  beskrivelse = lagOppgavebeskrivelse(behandlesAvApplikasjon, journalpost) ?: "",
                                  behandlingstype = settBehandlingstema(journalpost),
                                  behandlingstema = journalpost.behandlingstema,
                                  enhetsnummer = journalpost.journalforendeEnhet,
                                  behandlesAvApplikasjon = behandlesAvApplikasjon.applikasjon,
                                  tilordnetRessurs = tilordnet)

    fun toBehandleSakOppgave(journalpost: Journalpost, behandlesAvApplikasjon: BehandlesAvApplikasjon): OpprettOppgaveRequest =
            OpprettOppgaveRequest(ident = tilOppgaveIdent(journalpost),
                                  saksId = null,
                                  tema = Tema.ENF,
                                  oppgavetype = Oppgavetype.BehandleSak,
                                  journalpostId = journalpost.journalpostId,
                                  fristFerdigstillelse = lagFristForOppgave(LocalDateTime.now()),
                                  beskrivelse = lagOppgavebeskrivelse(behandlesAvApplikasjon, journalpost) ?: "",
                                  behandlingstema = journalpost.behandlingstema,
                                  enhetsnummer = null,
                                  behandlesAvApplikasjon = behandlesAvApplikasjon.applikasjon)

    private fun lagOppgavebeskrivelse(behandlesAvApplikasjon: BehandlesAvApplikasjon, journalpost: Journalpost): String? {
        if (journalpost.dokumenter.isNullOrEmpty()) error("Journalpost ${journalpost.journalpostId} mangler dokumenter")
        val dokumentTittel = journalpost.dokumenter!!.firstOrNull { it.brevkode != null }?.tittel ?: ""
        return "${behandlesAvApplikasjon.beskrivelsePrefix}$dokumentTittel"
    }

    private fun tilOppgaveIdent(journalpost: Journalpost): OppgaveIdentV2? {
        if (journalpost.bruker == null) {
            return null
        }

        return when (journalpost.bruker!!.type) {
            BrukerIdType.FNR -> {
                OppgaveIdentV2(ident = integrasjonerClient.hentAktørId(journalpost.bruker!!.id), gruppe = IdentGruppe.AKTOERID)
            }
            BrukerIdType.ORGNR -> OppgaveIdentV2(ident = journalpost.bruker!!.id, gruppe = IdentGruppe.ORGNR)
            BrukerIdType.AKTOERID -> OppgaveIdentV2(ident = journalpost.bruker!!.id, gruppe = IdentGruppe.AKTOERID)
        }
    }

    /**
     * Frist skal være 1 dag hvis den opprettes før kl. 12
     * og 2 dager hvis den opprettes etter kl. 12
     *
     * Helgedager må ekskluderes
     *
     */
    fun lagFristForOppgave(gjeldendeTid: LocalDateTime): LocalDate {
        val frist = when (gjeldendeTid.dayOfWeek) {
            DayOfWeek.FRIDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(2))
            DayOfWeek.SATURDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(2).withHour(8))
            DayOfWeek.SUNDAY -> fristBasertPåKlokkeslett(gjeldendeTid.plusDays(1).withHour(8))
            else -> fristBasertPåKlokkeslett(gjeldendeTid)
        }

        return when (frist.dayOfWeek) {
            DayOfWeek.SATURDAY -> frist.plusDays(2)
            DayOfWeek.SUNDAY -> frist.plusDays(1)
            else -> frist
        }
    }

    private fun settBehandlingstema(journalpost: Journalpost): String? {
        if (journalpost.tittel?.lowercase().equals("klage")) {
            logger.warn("Kunne ikke finne behandlingstema av journalpost ved opprettelse av oppgave")
            return KODEVERK_KLAGE
        }
        return null
    }

    private fun fristBasertPåKlokkeslett(gjeldendeTid: LocalDateTime): LocalDate {
        return if (gjeldendeTid.hour >= 12) {
            return gjeldendeTid.plusDays(2).toLocalDate()
        } else {
            gjeldendeTid.plusDays(1).toLocalDate()
        }
    }

}
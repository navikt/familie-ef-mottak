package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.kontrakter.felles.journalpost.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.oppgave.*
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class OpprettOppgaveMapper(private val integrasjonerClient: IntegrasjonerClient) {

    fun toDto(journalpost: Journalpost) =
            OpprettOppgaveRequest(ident = tilOppgaveIdent(journalpost),
                                  saksId = null,
                                  journalpostId = journalpost.journalpostId,
                                  tema = Tema.ENF,
                                  oppgavetype = Oppgavetype.Journalføring,
                                  fristFerdigstillelse = lagFristForOppgave(LocalDateTime.now()),
                                  beskrivelse = hentHoveddokumentTittel(journalpost) ?: "",
                                  behandlingstema = journalpost.behandlingstema,
                                  enhetsnummer = null)

    fun toBehandleSakOppgave(journalpost: Journalpost, behandlesAvApplikasjon: String) =
            OpprettOppgaveRequest(ident = tilOppgaveIdent(journalpost),
                                  saksId = null,
                                  tema = Tema.ENF,
                                  oppgavetype = Oppgavetype.BehandleSak,
                                  journalpostId = journalpost.journalpostId,
                                  fristFerdigstillelse = LocalDate.now().plusDays(2),
                                  beskrivelse = hentHoveddokumentTittel(journalpost) ?: "",
                                  behandlingstema = journalpost.behandlingstema,
                                  enhetsnummer = null,
                                  behandlesAvApplikasjon = behandlesAvApplikasjon
            )

    private fun hentHoveddokumentTittel(journalpost: Journalpost): String? {
        if (journalpost.dokumenter.isNullOrEmpty()) error("Journalpost ${journalpost.journalpostId} mangler dokumenter")
        return journalpost.dokumenter!!.firstOrNull { it.brevkode != null }?.tittel
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
     */
    fun lagFristForOppgave(gjeldendeTid: LocalDateTime): LocalDate {
        return if (gjeldendeTid.hour >= 12) {
            return gjeldendeTid.plusDays(2).toLocalDate()
        } else {
            gjeldendeTid.plusDays(1).toLocalDate()
        }
    }

}
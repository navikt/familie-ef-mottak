package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.kontrakter.felles.journalpost.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.oppgave.*
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class OpprettOppgaveMapper(private val integrasjonerClient: IntegrasjonerClient) {

    fun toDto(journalpost: Journalpost) = OpprettOppgave(ident = tilOppgaveIdent(journalpost),
                                                         saksId = null,
                                                         journalpostId = journalpost.journalpostId,
                                                         tema = Tema.ENF,
                                                         oppgavetype = Oppgavetype.Journalføring,
                                                         fristFerdigstillelse = LocalDate.now(),
                                                         beskrivelse = hentHoveddokumentTittel(journalpost) ?: "",
                                                         behandlingstema = journalpost.behandlingstema,
                                                         enhetsnummer = null)

    fun toBehandleSakOppgave(journalpost: Journalpost, saksBlokk: String, saksId: String) = OpprettOppgave(ident = tilOppgaveIdent(journalpost),
                                                         saksId = saksId,
                                                         tema = Tema.ENF,
                                                         oppgavetype = Oppgavetype.BehandleSak,
                                                         fristFerdigstillelse = LocalDate.now().plusDays(2),
                                                         beskrivelse = "${hentHoveddokumentTittel(journalpost)} - Saksblokk: $saksId" ?: "",
                                                         behandlingstema = journalpost.behandlingstema,
                                                         enhetsnummer = null)

    private fun hentHoveddokumentTittel(journalpost: Journalpost): String? {
        if (journalpost.dokumenter.isNullOrEmpty()) error("Journalpost ${journalpost.journalpostId} mangler dokumenter")
        return journalpost.dokumenter!!.firstOrNull { it.brevkode != null }?.tittel
    }

    private fun tilOppgaveIdent(journalpost: Journalpost): OppgaveIdent? {
        if (journalpost.bruker == null) {
            return null
        }

        return when (journalpost.bruker!!.type) {
            BrukerIdType.FNR -> {
                OppgaveIdent(ident = integrasjonerClient.hentAktørId(journalpost.bruker!!.id), type = IdentType.Aktør)
            }
            BrukerIdType.ORGNR -> OppgaveIdent(ident = journalpost.bruker!!.id, type = IdentType.Organisasjon)
            BrukerIdType.AKTOERID -> OppgaveIdent(ident = journalpost.bruker!!.id, type = IdentType.Aktør)
        }
    }


}
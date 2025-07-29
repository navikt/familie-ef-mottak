package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.util.DatoUtil
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

@Table("soknad")
data class Søknad(
    @Id
    val id: String = UUID.randomUUID().toString(),
    @Column("soknad_pdf")
    val søknadPdf: EncryptedFile? = null,
    val dokumenttype: String,
    val journalpostId: String? = null,
    val saksnummer: String? = null,
    val fnr: String,
    val taskOpprettet: Boolean = false,
    val opprettetTid: LocalDateTime = DatoUtil.dagensDatoMedTid(),
    val behandleINySaksbehandling: Boolean = false,
    val json: String,
)

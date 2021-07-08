package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.FileCryptoConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoConverter
import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ettersending")
data class Ettersending(
        @Id
        val id: String = UUID.randomUUID().toString(),
        @Convert(converter = StringValCryptoConverter::class)
        @Column(name = "ettersending_json")
        val ettersendingJson: String,
        @Convert(converter = FileCryptoConverter::class)
        @Column(name = "ettersending_pdf")
        val ettersendingPdf: Fil? = null,
        val dokumenttype: String,
        @Column(name = "journalpost_id")
        val journalpostId: String? = null,
        val saksnummer: String? = null,
        val fnr: String,
        val taskOpprettet: Boolean = false,
        @Column(name = "opprettet_tid")
        val opprettetTid: LocalDateTime = LocalDateTime.now(),
)
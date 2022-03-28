package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.EncryptedString
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.UUID

data class Ettersending(
        @Id
        val id: UUID = UUID.randomUUID(),
        val ettersendingJson: EncryptedString,
        val ettersendingPdf: EncryptedFile? = null,
        @Column("stonad_type")
        val stønadType: String,
        val journalpostId: String? = null,
        val fnr: String,
        val taskOpprettet: Boolean = false,
        val opprettetTid: LocalDateTime = LocalDateTime.now(),
)
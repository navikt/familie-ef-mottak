package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.EncryptedString
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
        val id: UUID = UUID.randomUUID(),
        @Convert(converter = StringValCryptoConverter::class)
        val ettersendingJson: EncryptedString,
        @Convert(converter = FileCryptoConverter::class)
        val ettersendingPdf: EncryptedFile? = null,
        @Column(name = "stonad_type")
        val stønadType: String,
        val journalpostId: String? = null,
        val fnr: String,
        val taskOpprettet: Boolean = false,
        val opprettetTid: LocalDateTime = LocalDateTime.now(),
)
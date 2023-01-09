package no.nav.familie.ef.mottak.repository.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.util.UUID

data class Vedlegg(
    @Id
    val id: UUID,
    @Column("soknad_id")
    val søknadId: String,
    val navn: String,
    val tittel: String,
    val innhold: EncryptedFile,
)

package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.prosessering.domene.PropertiesToStringConverter
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "HENDELSESLOGG")
data class Hendelseslogg(
        @Column(name = "kafka_offset")
        val offset: Long,

        @Column(name = "hendelse_id")
        val hendelseId: String,

        @Convert(converter = PropertiesToStringConverter::class)
        @Column(name = "metadata")
        val metadata: Properties = Properties(),

        @Id
        val id: UUID = UUID.randomUUID(),


        @Column(name = "opprettet_tid", nullable = false, updatable = false)
        val opprettetTidspunkt: LocalDateTime = LocalDateTime.now(),

        @Column(name = "ident", nullable = true)
        val ident: String? = null
)

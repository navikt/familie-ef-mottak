package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.prosessering.domene.PropertiesWrapper
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.Properties
import java.util.UUID

data class Hendelseslogg(
    @Column("kafka_offset")
    val offset: Long,

    @Column("hendelse_id")
    val hendelseId: String,

    @Column("metadata")
    val metadataWrapper: PropertiesWrapper = PropertiesWrapper(Properties()),

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column("opprettet_tid")
    val opprettetTidspunkt: LocalDateTime = LocalDateTime.now(),

    @Column("ident")
    val ident: String? = null,
) {

    @Transient
    val metadata: Properties = metadataWrapper.properties
}

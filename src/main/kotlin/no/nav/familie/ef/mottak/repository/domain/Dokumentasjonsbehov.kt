package no.nav.familie.ef.mottak.repository.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Dokumentasjonsbehov(
    @Id
    @Column("soknad_id")
    val søknadId: String,
    val data: String
)

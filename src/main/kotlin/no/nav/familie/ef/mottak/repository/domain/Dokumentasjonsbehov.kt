package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Dokumentasjonsbehov(@Id
                               val id: UUID = UUID.randomUUID(),
                               @Column(name = "soknad_id")
                               val søknadId: String,
                               val data: List<Dokumentasjonsbehov>)

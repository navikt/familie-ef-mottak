package no.nav.familie.ef.mottak.repository.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Dokumentasjonsbehov(@Id
                               @Column(name = "soknad_id")
                               val søknadId: String,
                               val data: String)

package no.nav.familie.ef.mottak.repository.domain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class EttersendingDokumentasjonsbehov(@Id
                                           @Column(name = "ettersending_id")
                                           val ettersendingId: String,
                                           val data: String)

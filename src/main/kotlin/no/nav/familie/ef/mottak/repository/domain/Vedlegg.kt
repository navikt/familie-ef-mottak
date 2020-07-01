package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.FileCryptoConverter
import java.util.*
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Vedlegg(@Id
                   val id: UUID = UUID.randomUUID(),
                   @Column(name = "soknad_id")
                   val søknadId: String,
                   val navn: String,
                   val tittel: String,
                   @Convert(converter = FileCryptoConverter::class)
                   val innhold: Fil)

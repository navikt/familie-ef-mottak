package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.FileCryptoConverter
import javax.persistence.*

@Entity
@Table
data class Vedlegg(@Id
                   @GeneratedValue(strategy = GenerationType.IDENTITY)
                   val id: Long? = null,
                   @Convert(converter = FileCryptoConverter::class)
                   val data: Fil,
                   val filnavn: String)

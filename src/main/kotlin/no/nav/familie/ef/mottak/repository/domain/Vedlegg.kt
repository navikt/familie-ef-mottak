package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.PdfCryptoConverter
import javax.persistence.*

@Entity
@Table
data class Vedlegg(@Id
                   @GeneratedValue(strategy = GenerationType.IDENTITY)
                   val id: Long? = null,
                   @Convert(converter = PdfCryptoConverter::class)
                   val data: Pdf,
                   val filnavn: String)

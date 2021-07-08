package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.FileCryptoConverter
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.PreUpdate

@Entity
data class EttersendingVedlegg(@Id
                   val id: UUID,
                   @Column(name = "ettersending_id")
                   val ettersendingId: String,
                   val navn: String,
                   val tittel: String,
                   @Convert(converter = FileCryptoConverter::class)
                   val innhold: Fil) {

    @PreUpdate private fun preUpdate() {
        throw UnsupportedOperationException(UPDATE_FEILMELDING)
    }

    companion object {

        const val UPDATE_FEILMELDING: String = "Det går ikke å oppdatere vedlegg";
    }
}

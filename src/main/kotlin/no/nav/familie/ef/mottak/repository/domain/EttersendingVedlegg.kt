package no.nav.familie.ef.mottak.repository.domain

import org.springframework.data.annotation.Id
import java.util.UUID

data class EttersendingVedlegg(
    @Id
    val id: UUID,
    val ettersendingId: UUID,
    val navn: String,
    val tittel: String,
    val innhold: EncryptedFile
) {

    companion object {

        const val UPDATE_FEILMELDING: String = "Det går ikke å oppdatere vedlegg"
    }
}

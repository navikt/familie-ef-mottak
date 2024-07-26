package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.encryption.EncryptedString
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper

object EttersendingMapper {
    inline fun <reified T : Any> toDto(ettersending: Ettersending): T = objectMapper.readValue(ettersending.ettersendingJson.data)

    fun fromDto(
        stønadType: StønadType,
        ettersending: EttersendelseDto,
    ): Ettersending =
        Ettersending(
            ettersendingJson = EncryptedString(objectMapper.writeValueAsString(ettersending)),
            fnr = ettersending.personIdent,
            stønadType = stønadType.toString(),
        )
}

package no.nav.familie.ef.mottak.mapper

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.objectMapper

object VedleggMapper {

    fun toDto(soknad: Soknad): List<Vedlegg> =
            if (soknad.vedlegg != null) objectMapper.readValue(soknad.vedlegg) else emptyList()

}

package no.nav.familie.ef.mottak.util

import no.nav.familie.ef.mottak.api.ApiFeil
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.springframework.http.HttpStatus

fun okEllerKastException(producer: () -> Kvittering): Kvittering {
    try {
        return producer.invoke()
    } catch (e: Exception) {
        if (e.getRootCause()?.message == Vedlegg.UPDATE_FEILMELDING) {
            throw ApiFeil("Det går ikke å sende inn samme vedlegg to ganger", HttpStatus.BAD_REQUEST)
        } else {
            throw e
        }
    }
}
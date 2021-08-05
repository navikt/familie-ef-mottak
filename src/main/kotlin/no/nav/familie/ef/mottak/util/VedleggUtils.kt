package no.nav.familie.ef.mottak.util

import no.nav.familie.ef.mottak.api.ApiFeil
import no.nav.familie.ef.mottak.api.dto.Kvittering
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

fun okEllerKastException(producer: () -> Kvittering): ResponseEntity<Kvittering> {
    try {
        return ResponseEntity.ok(producer.invoke())
    } catch (e: Exception) {
        if (e.getRootCause()?.message == no.nav.familie.ef.mottak.repository.domain.Vedlegg.UPDATE_FEILMELDING) {
            throw ApiFeil("Det går ikke å sende inn samme vedlegg to ganger", HttpStatus.BAD_REQUEST)
        } else {
            throw e
        }
    }
}
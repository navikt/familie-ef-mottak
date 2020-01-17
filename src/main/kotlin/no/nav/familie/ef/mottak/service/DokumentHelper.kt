package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Dokument
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

object DokumentHelper {

    fun finnDokumenter(entity: Any): List<Dokument> {

        if (entity is Dokument) {
            return listOf(entity)
        }

        if (entity is List<Any?>) {
            return entity.filterNotNull().map { finnDokumenter(it) }.flatten()
        }

        return entity::class.memberProperties
                .asSequence()
                .filter { it.visibility == KVisibility.PUBLIC }
                .map { it.getter.call(entity) }
                .filterNotNull()
                .map { finnDokumenter(it) }
                .flatten()
                .toList()

    }


}
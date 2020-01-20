package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Felt
import no.nav.familie.kontrakter.ef.søknad.Søknad
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

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


    fun finnFelter(søknad: Søknad): List<List<Felt<*>>> {

        val parameters = søknad::class.primaryConstructor!!.parameters

        return parameters.map { param -> søknad::class.declaredMemberProperties.first { it.name == param.name } }
                .mapNotNull { it.getter.call(søknad) }
                .map { finnFelter(it) }
                .toList()


    }

    fun finnFelter(entity: Any): List<Felt<*>> {

        if (entity is List<Any?>) {
            return entity.filterNotNull().map { finnFelter(it) }.flatten()
        }

        val parameters = entity::class.primaryConstructor?.parameters ?: return emptyList()

        val list = parameters
                .asSequence()
                .map { param -> entity::class.declaredMemberProperties.first { it.name == param.name } }
                .filterNotNull()
                .mapNotNull { it.getter.call(entity) }
                .filterNotNull()
                .map { finnFelter(it) }
                .filterNotNull()
                .flatten()
                .toList()
        return if (entity is Felt<*>) list + entity else list

    }


}
package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Felt
import no.nav.familie.kontrakter.ef.søknad.Fødselsnummer
import no.nav.familie.kontrakter.ef.søknad.Søknad
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor


object SøknadTreeWalker {


    private val endNodes =
            setOf<KClass<*>>(String::class,
                             Int::class,
                             Boolean::class,
                             Dokument::class,
                             Fødselsnummer::class,
                             LocalDate::class,
                             Long::class)


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


    fun finnFelter(søknad: Søknad): Map<String, List<Felt<String>>> {

        val listFeltverdier = listFeltverdier(søknad).filterIsInstance<Felt<*>>()


        return listFeltverdier.associateBy( {it.label}, {finnFelter(it)} )

    }

    private fun finnFelter(entity: Any): List<Felt<String>> {

        if (entity is ByteArray) {
            return emptyList()
        }

        if (entity is List<Any?>) {
            return entity.filterNotNull()
                    .map { finnFelter(it) }
                    .flatten()
        }


        val list = listFeltverdier(entity)
                .map { finnFelter(it) }
                .flatten()
                .toList()


        return if (entity is Felt<*>) {
            val verdi = entity.verdi!!
            if (verdi::class in endNodes) {
                return listOf(Felt(entity.label, entity.verdi.toString()))
            } else {
                listOf(Felt(entity.label, "seksjon")) + list
            }
        } else list

    }

    private fun listFeltverdier(entity: Any): List<Any> {
        val parameters = entity::class.primaryConstructor?.parameters
                         ?: return emptyList()
        return parameters
                .asSequence()
                .map { param -> entity::class.declaredMemberProperties.first { it.name == param.name } }
                .filterNotNull()
                .filter { it.visibility == KVisibility.PUBLIC }
                .mapNotNull { it.getter.call(entity) }
                .filterNotNull()
                .toList()
    }
}
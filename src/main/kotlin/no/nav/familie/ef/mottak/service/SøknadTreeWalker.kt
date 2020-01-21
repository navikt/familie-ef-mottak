package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Felt
import no.nav.familie.kontrakter.ef.søknad.Fødselsnummer
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*
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


    fun finnFelter(entity: Any): List<Felt<*>> {


        if (entity is ByteArray) {
            return emptyList()
        }

        if (entity is List<Any?>) {
            return entity.filterNotNull()
                    .map { finnFelter(it) }
                    .flatten()
        }
        val parameters = entity::class.primaryConstructor?.parameters
                         ?: return emptyList()
        val list = parameters
                .asSequence()
                .map { param -> entity::class.declaredMemberProperties.first { it.name == param.name } }
                .filterNotNull()
                .filter { it.visibility == KVisibility.PUBLIC }
                .mapNotNull { it.getter.call(entity) }
                .filterNotNull()
                .map { finnFelter(it) }
                .flatten()
                .toList()

        return if (entity is Felt<*>) {
            val verdi = entity.verdi!!
            if (verdi::class in endNodes) {
                return when (verdi::class) {
                    Month::class ->
                        listOf(Felt(entity.label, (entity.verdi as Month).getDisplayName(TextStyle.FULL, Locale("no"))))
                    Boolean::class ->
                        listOf(Felt(entity.label, if(entity.verdi as Boolean) "Ja" else "Nei"))
                    else ->
                        listOf(Felt(entity.label, entity.verdi.toString()))
                }
            } else {
                listOf(Felt(entity.label, list))
            }
        } else list

    }
}
package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.Dokument
import no.nav.familie.kontrakter.ef.søknad.Felt
import no.nav.familie.kontrakter.ef.søknad.Fødselsnummer
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
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
                             Month::class,
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

    fun finnFelter(entitet: Any): List<Felt<*>> {

        // Kotlin reflection takler ikke å kalle getter på size, fordi den ikke finnes. Så vi ignorerer dem her.
        if (entitet is ByteArray) {
            return emptyList()
        }

        // Det går ike å hente elementene i en liste med reflection, så vi traverserer den som vanlig.
        if (entitet is List<Any?>) {
            return entitet.filterNotNull()
                    .map { finnFelter(it) }
                    .flatten()
        }
        val parametere = kontruktørparametere(entitet)

        val list = parametere
                .asSequence()
                .map { finnFelt(entitet, it) }
                .filter { it.visibility == KVisibility.PUBLIC }
                .mapNotNull { getFeltverdi(it, entitet) }
                .map { finnFelter(it) } // Kall rekursivt videre
                .flatten()
                .toList()

        if (entitet is Felt<*>) {
            if (entitet.verdi!!::class in endNodes) {
                return mapEndenodeTilFelt(entitet)
            }
            if (entitet.verdi is List<*>) {
                val list1 = entitet.verdi as List<*>
                if (list1.isNotEmpty() && list1.first() is String) {
                    return mapEndenodeTilFelt(entitet)
                }

            }
            return listOf(Felt(entitet.label, list))
        }
        return list
    }

    /**
     * Håndterer formatering utover vanlig toString for endenodene
     */
    private fun mapEndenodeTilFelt(entitet: Felt<*>): List<Felt<*>> {
        val verdi = entitet.verdi!!

        return when (verdi) {
            is Month -> listOf(Felt(entitet.label, verdi.getDisplayName(TextStyle.FULL, Locale("no"))))
            is Boolean -> listOf(Felt(entitet.label, if (verdi) "Ja" else "Nei"))
            is List<*> -> listOf(Felt(entitet.label, verdi.joinToString()))
            is Fødselsnummer -> listOf(Felt(entitet.label, verdi.verdi))
            is Dokument -> listOf(Felt(entitet.label, verdi.tittel))
            else -> listOf(Felt(entitet.label, verdi.toString()))

        }
    }

    /**
     * Henter ut verdien for felt på entitet.
     */
    private fun getFeltverdi(felt: KProperty1<out Any, Any?>, entitet: Any) =
            felt.getter.call(entitet)

    /**
     * Finn første (og eneste) felt på entiteten som har samme navn som konstruktørparameter.
     */
    private fun finnFelt(entity: Any, konstruktørparameter: KParameter) =
            entity::class.declaredMemberProperties.first { it.name == konstruktørparameter.name }

    /**
     * Konstruktørparametere er det eneste som gir oss en garantert rekkefølge for feltene, så vi henter disse først.
     */
    fun kontruktørparametere(entity: Any) = entity::class.primaryConstructor?.parameters ?: emptyList()

}
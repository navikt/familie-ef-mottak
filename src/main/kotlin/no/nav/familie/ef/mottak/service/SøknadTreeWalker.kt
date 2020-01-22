package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.*
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
                             Adresse::class,
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

    fun mapSøknadsfelterTilMap(søknad: Søknad): Map<String, Any> {

        val finnFelter = finnFelter(søknad)

        return feltlisteMap("søknad", finnFelter)
    }


    private fun finnFelter(entitet: Any): List<Map<String, *>> {

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
        val parametere = konstruktørparametere(entitet)

        val list = parametere
                .asSequence()
                .map { finnSøknadsfelt(entitet, it) }
                .filter { it.visibility == KVisibility.PUBLIC }
                .mapNotNull { getFeltverdi(it, entitet) }
                .map { finnFelter(it) } // Kall rekursivt videre
                .flatten()
                .toList()

        if (entitet is Søknadsfelt<*>) {
            if (entitet.verdi!!::class in endNodes) {
                return listOf(mapEndenodeTilUtskriftMap(entitet))
            }
            if (entitet.verdi is List<*>) {
                val verdiliste = entitet.verdi as List<*>
                if (verdiliste.isNotEmpty() && verdiliste.first() is String) {
                    return listOf(mapEndenodeTilUtskriftMap(entitet))
                }

            }
            return listOf(feltlisteMap(entitet.label, list))
        }
        return list
    }

    /**
     * Håndterer formatering utover vanlig toString for endenodene
     */
    private fun mapEndenodeTilUtskriftMap(entitet: Søknadsfelt<*>): Map<String, String> {

        return when (val verdi = entitet.verdi!!) {
            is Month ->
                feltMap(entitet.label, verdi.getDisplayName(TextStyle.FULL, Locale("no")))
            is Boolean ->
                feltMap(entitet.label, if (verdi) "Ja" else "Nei")
            is List<*> ->
                feltMap(entitet.label, verdi.joinToString("\n"))
            is Fødselsnummer ->
                feltMap(entitet.label, verdi.verdi)
            is Dokument ->
                feltMap(entitet.label, verdi.tittel)
            is Adresse ->
                feltMap(entitet.label, adresseString(verdi))
            else ->
                feltMap(entitet.label, verdi.toString())

        }
    }

    private fun adresseString(adresse: Adresse): String {
        return listOf(listOf(adresse.gatenavn, adresse.husnummer, adresse.husbokstav).joinToString(),
                      adresse.bolignummer,
                      listOf(adresse.postnummer, adresse.poststedsnavn).joinToString()).joinToString("\n\n")
    }

    private fun feltMap(label: String, verdi: String) = mapOf("label" to label, "verdi" to verdi)

    private fun feltlisteMap(label: String, verdi: List<*>) = mapOf("label" to label, "verdiliste" to verdi)

    /**
     * Henter ut verdien for felt på entitet.
     */
    private fun getFeltverdi(felt: KProperty1<out Any, Any?>, entitet: Any) =
            felt.getter.call(entitet)

    /**
     * Finn første (og eneste) felt på entiteten som har samme navn som konstruktørparameter.
     */
    private fun finnSøknadsfelt(entity: Any, konstruktørparameter: KParameter) =
            entity::class.declaredMemberProperties.first { it.name == konstruktørparameter.name }

    /**
     * Konstruktørparametere er det eneste som gir oss en garantert rekkefølge for feltene, så vi henter disse først.
     */
    private fun konstruktørparametere(entity: Any) = entity::class.primaryConstructor?.parameters ?: emptyList()

}

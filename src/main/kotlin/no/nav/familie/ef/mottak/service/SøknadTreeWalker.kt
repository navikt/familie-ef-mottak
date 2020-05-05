package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.*
import java.time.LocalDate
import java.time.Month
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
                             Periode::class,
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

    fun mapSøknadsfelter(søknad: Søknad): Map<String, Any> {
        val finnFelter = finnFelter(søknad)
        return feltlisteMap("Søknad enslig forsørger", finnFelter)
    }

    fun mapSkjemafelter(skjema: SkjemaForArbeidssøker): Map<String, Any> {
        val finnFelter = finnFelter(skjema)
        return feltlisteMap("Skjema for arbeidssøker - 15-08.01", finnFelter)
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
                return listOf(Feltformaterer.mapEndenodeTilUtskriftMap(entitet))
            }
            if (entitet.verdi is List<*>) {
                val verdiliste = entitet.verdi as List<*>
                if (verdiliste.isNotEmpty() && verdiliste.first() is String) {
                    return listOf(Feltformaterer.mapEndenodeTilUtskriftMap(entitet))
                }

            }
            return listOf(feltlisteMap(entitet.label, list))
        }
        return list
    }

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

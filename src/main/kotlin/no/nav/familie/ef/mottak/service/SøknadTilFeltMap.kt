package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.FeltMap
import no.nav.familie.ef.mottak.repository.domain.PdfConfig
import no.nav.familie.ef.mottak.repository.domain.VerdilisteElement
import no.nav.familie.kontrakter.ef.søknad.Adresse
import no.nav.familie.kontrakter.ef.søknad.Barn
import no.nav.familie.kontrakter.ef.søknad.Datoperiode
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjon
import no.nav.familie.kontrakter.ef.søknad.MånedÅrPeriode
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.ef.søknad.Utenlandsopphold
import no.nav.familie.kontrakter.felles.Fødselsnummer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

object SøknadTilFeltMap {
    private val endNodes =
        setOf<KClass<*>>(
            String::class,
            Int::class,
            Boolean::class,
            Double::class,
            Dokumentasjon::class,
            Fødselsnummer::class,
            MånedÅrPeriode::class,
            Datoperiode::class,
            Adresse::class,
            LocalDate::class,
            LocalDateTime::class,
            Month::class,
            Long::class,
        )

    fun mapOvergangsstønad(
        søknad: SøknadOvergangsstønad,
        vedleggTitler: List<String>,
    ): FeltMap {
        val finnFelter = finnFelter(søknad)
        val vedlegg = mapTilVedlegg(vedleggTitler)
        val språk = søknad.innsendingsdetaljer.verdi.språk ?: "nb"
        return FeltMap(
            "Søknad om overgangsstønad",
            finnFelter + vedlegg,
            PdfConfig(true, språk),
            getSkjemanummerTekst("overgangsstønad", språk),
        )
    }

    fun mapBarnetilsyn(
        søknad: SøknadBarnetilsyn,
        vedleggTitler: List<String>,
    ): FeltMap {
        val finnFelter = finnFelter(søknad)
        val vedlegg = mapTilVedlegg(vedleggTitler)
        val språk = søknad.innsendingsdetaljer.verdi.språk ?: "nb"
        return FeltMap(
            "Søknad om stønad til barnetilsyn",
            finnFelter + vedlegg,
            PdfConfig(
                true,
                språk,
            ),
            getSkjemanummerTekst("barnetilsyn", språk),
        )
    }

    fun mapSkolepenger(
        søknad: SøknadSkolepenger,
        vedleggTitler: List<String>,
    ): FeltMap {
        val finnFelter = finnFelter(søknad)
        val vedlegg = mapTilVedlegg(vedleggTitler)
        val språk = søknad.innsendingsdetaljer.verdi.språk ?: "nb"
        return FeltMap(
            "Søknad om stønad til skolepenger",
            finnFelter + vedlegg,
            PdfConfig(true, språk),
            getSkjemanummerTekst("skolepenger", språk),
        )
    }

    fun mapSkjemafelter(skjema: SkjemaForArbeidssøker): FeltMap {
        val finnFelter = finnFelter(skjema)
        val språk = skjema.innsendingsdetaljer.verdi.språk ?: "nb"
        return FeltMap(
            "Skjema for arbeidssøker",
            finnFelter,
            PdfConfig(false, språk),
            getSkjemanummerTekst("arbeidssøker", språk),
        )
    }

    fun mapEttersending(
        ettersending: Ettersending,
        vedleggTitler: List<String>,
    ): FeltMap {
        val infoMap =
            VerdilisteElement(
                label = "Ettersending av vedlegg",
                verdiliste =
                    listOf(
                        VerdilisteElement("Stønadstype", verdi = ettersending.stønadType),
                        VerdilisteElement("Fødselsnummer", verdi = ettersending.fnr),
                        VerdilisteElement(
                            "Dato mottatt",
                            verdi = ettersending.opprettetTid.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                        ),
                    ),
            )
        val vedleggMap = mapTilVedlegg(vedleggTitler, "Dokumenter vedlagt")
        return FeltMap("Ettersending", verdiliste = listOf(infoMap, vedleggMap))
    }

    private fun finnFelter(entitet: Any): List<VerdilisteElement> {
        // Det går ikke å hente elementene i en liste med reflection, så vi traverserer den som vanlig.
        if (entitet is List<Any?>) {
            return entitet
                .filterNotNull()
                .map { finnFelter(it) }
                .flatten()
        }
        val parametere = konstruktørparametere(entitet)

        val list =
            parametere
                .asSequence()
                .map { finnSøknadsfelt(entitet, it) }
                .filter { it.visibility == KVisibility.PUBLIC }
                .mapNotNull { getFeltverdi(it, entitet) }
                .map { finnFelter(it) } // Kall rekursivt videre
                .flatten()
                .toList()

        if (entitet is Søknadsfelt<*>) {
            val erBarn = (entitet.label == "Barna dine" || entitet.label == "Your children") && entitet.verdi is List<*>
            val erArbeidsforhold =
                (entitet.label == "Om arbeidsforholdet ditt" || entitet.label == "About your employment") && entitet.verdi is List<*>
            val erUtenlandsopphold = entitet.label == "Utenlandsopphold" && entitet.verdi is List<*>
            if (entitet.verdi!! is Dokumentasjon) {
                @Suppress("UNCHECKED_CAST")
                return mapDokumentasjon(entitet as Søknadsfelt<Dokumentasjon>)
            }
            if (entitet.verdi!!::class in endNodes) {
                return FeltformatererPdfKvittering.genereltFormatMapperMapEndenode(entitet)?.let { listOf(it) }
                    ?: emptyList()
            }
            if (erBarn || erArbeidsforhold || erUtenlandsopphold) {
                return mapListeElementer(entitet)
            }
            if (entitet.alternativer != null) {
                return mapAlternativerOgSvar(entitet)
            }
            if (entitet.verdi is List<*>) {
                val verdiliste = entitet.verdi as List<*>

                if (verdiliste.firstOrNull() is String) {
                    return FeltformatererPdfKvittering.genereltFormatMapperMapEndenode(entitet)?.let { listOf(it) }
                        ?: emptyList()
                }
            }
            // skal ekskluderes
            if (list.size == 1 && list.first().verdiliste.isNullOrEmpty() && list.first().verdi.isNullOrEmpty()) {
                return emptyList()
            }

            return listOf(VerdilisteElement(label = entitet.label, verdiliste = list))
        }
        return list
    }

    private fun mapDokumentasjon(entitet: Søknadsfelt<Dokumentasjon>): List<VerdilisteElement> {
        val list =
            listOf(FeltformatererPdfKvittering.genereltFormatMapperMapEndenode(entitet.verdi.harSendtInnTidligere))
        if (list.size == 1 && list.first()?.verdiliste.isNullOrEmpty() && list.first()?.verdi.isNullOrEmpty()) {
            return emptyList()
        }
        return listOf(VerdilisteElement(label = entitet.label, verdiliste = list.filterNotNull()))
    }

    private fun mapListeElementer(
        entitet: Søknadsfelt<*>,
    ): List<VerdilisteElement> {
        val mappedElementer =
            (entitet.verdi as List<*>).mapIndexedNotNull { indeks, it ->
                when (it) {
                    is Barn -> mapBarnElementer(entitet.label, indeks, it)
                    is no.nav.familie.kontrakter.ef.søknad.Arbeidsgiver ->
                        mapArbeidsforholdElementer(
                            entitet.label,
                            indeks,
                            it,
                        )

                    is Utenlandsopphold -> mapUtenlandsoppholdElementer(entitet.label, indeks, it)
                    else -> null
                }
            }
        return listOf(
            VerdilisteElement(
                entitet.label,
                verdiliste = mappedElementer,
                visningsVariant = VisningsVariant.TABELL.toString(),
            ),
        )
    }

    private fun mapBarnElementer(
        elementLabel: String,
        indeks: Int,
        barn: Barn,
    ): VerdilisteElement? {
        val element = if (elementLabel == "Barna dine") "Barn" else "Child"
        val tabellCaption = "$element ${indeks + 1}"
        val verdilisteElementListe = finnFelter(barn).filterNot { it.verdi == "" && it.verdiliste.isNullOrEmpty() }
        return verdilisteElementListe.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(label = tabellCaption, verdiliste = it)
        }
    }

    private fun mapArbeidsforholdElementer(
        elementLabel: String,
        indeks: Int,
        arbeidsforhold: no.nav.familie.kontrakter.ef.søknad.Arbeidsgiver,
    ): VerdilisteElement? {
        val element = if (elementLabel == "Om arbeidsforholdet ditt") "Arbeidsforhold" else "Employment"
        val tabellCaption = "$element ${indeks + 1}"
        val verdilisteElementListe = finnFelter(arbeidsforhold)
        return verdilisteElementListe.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(label = tabellCaption, verdiliste = it)
        }
    }

    private fun mapUtenlandsoppholdElementer(
        elementLabel: String,
        indeks: Int,
        utenlandsopphold: Utenlandsopphold,
    ): VerdilisteElement? {
        val element = "Utenlandsopphold"
        val tabellCaption = "$element ${indeks + 1}"
        val verdilisteElementListe = finnFelter(utenlandsopphold)
        return verdilisteElementListe.takeIf { it.isNotEmpty() }?.let {
            VerdilisteElement(label = tabellCaption, verdiliste = it)
        }
    }

    private fun mapAlternativerOgSvar(entitet: Søknadsfelt<*>): List<VerdilisteElement> {
        val alternativListe = mutableListOf<VerdilisteElement>()
        val svarListe = mutableListOf<VerdilisteElement>()

        if (entitet.verdi is List<*> && entitet.alternativer is List<*>) {
            val alternativer = entitet.alternativer as List<String>
            val svarene = entitet.verdi as List<String>

            alternativer.forEach { alternativ ->
                alternativListe.add(VerdilisteElement(alternativ))
            }
            svarene.forEach { svar ->
                svarListe.add(VerdilisteElement(svar))
            }
        }

        val svaralternativTittel =
            if (entitet.label == "Does any of the following apply to you?") "Answer options" else "Svaralternativ"
        val alternativerElement =
            VerdilisteElement(
                label = svaralternativTittel,
                visningsVariant = VisningsVariant.PUNKTLISTE.toString(),
                verdiliste = alternativListe,
            )

        val svarTittel = if (entitet.label == "Does any of the following apply to you?") "Answer" else "Svar"
        val svarElement =
            VerdilisteElement(
                label = svarTittel,
                visningsVariant = VisningsVariant.PUNKTLISTE.toString(),
                verdiliste = svarListe,
            )

        return listOf(
            VerdilisteElement(
                label = entitet.label,
                verdiliste = listOf(alternativerElement, svarElement),
            ),
        )
    }

    /**
     * Henter ut verdien for felt på entitet.
     */
    private fun getFeltverdi(
        felt: KProperty1<out Any, Any?>,
        entitet: Any,
    ) = felt.getter.call(entitet)

    /**
     * Finn første (og eneste) felt på entiteten som har samme navn som konstruktørparameter.
     */
    private fun finnSøknadsfelt(
        entity: Any,
        konstruktørparameter: KParameter,
    ) = entity::class.declaredMemberProperties.first { it.name == konstruktørparameter.name }

    /**
     * Konstruktørparametere er det eneste som gir oss en garantert rekkefølge for feltene, så vi henter disse først.
     */
    private fun konstruktørparametere(entity: Any) = entity::class.primaryConstructor?.parameters ?: emptyList()

    private fun mapTilVedlegg(
        vedleggTitler: List<String>,
        label: String = "Vedlegg",
    ) = VerdilisteElement(
        label,
        verdiliste = listOf(FeltformatererPdfKvittering.mapVedlegg(vedleggTitler)),
        visningsVariant = VisningsVariant.VEDLEGG.toString(),
    )
}

enum class VisningsVariant {
    TABELL,
    VEDLEGG,
    PUNKTLISTE,
}

private fun getSkjemanummerTekst(
    skjemanummer: String,
    språk: String,
): String? {
    val skjemanumre =
        mapOf(
            "overgangsstønad" to
                mapOf(
                    "nb" to "Skjemanummer: NAV 15-00.01",
                    "en" to "Application number: NAV 15-00.01",
                ),
            "barnetilsyn" to mapOf("nb" to "Skjemanummer: NAV 15-00.02", "en" to "Application number: NAV 15-00.02"),
            "skolepenger" to mapOf("nb" to "Skjemanummer: NAV 15-00.04", "en" to "Application number: NAV 15-00.04"),
            "arbeidssøker" to mapOf("nb" to "Skjemanummer: NAV 15-08.01", "en" to "Application number: NAV 15-08.01"),
        )
    return skjemanumre[skjemanummer]?.get(språk)
}

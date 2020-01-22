package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.felles.objectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class SøknadTreeWalkerTest {

    @Test
    fun `finnDokumenter finner alle dokumenter i en struktur`() {

        val søknad = søknad()

        val list: List<Dokument> = SøknadTreeWalker.finnDokumenter(søknad)

        assertThat(list).contains(Dokument(Fil(byteArrayOf(12)), "giftIUtlandetDokumentasjon"),
                                  Dokument(Fil(byteArrayOf(12)), "separertEllerSkiltIUtlandetDokumentasjon"),
                                  Dokument(Fil(byteArrayOf(12)), "separasjonsbekreftelse"),
                                  Dokument(Fil(byteArrayOf(12)), "samlivsbruddsdokumentasjon"),
                                  Dokument(Fil(byteArrayOf(12)), "flyktningdokumentasjon"),
                                  Dokument(Fil(byteArrayOf(12)), "avtaleOmDeltBosted"),
                                  Dokument(Fil(byteArrayOf(12)), "samværsavtale"),
                                  Dokument(Fil(byteArrayOf(12)), "erklæringOmSamlivsbrudd"),
                                  Dokument(Fil(byteArrayOf(12)), "terminbekreftelse"),
                                  Dokument(Fil(byteArrayOf(12)), "sykdom"),
                                  Dokument(Fil(byteArrayOf(12)), "barnsSykdom"),
                                  Dokument(Fil(byteArrayOf(12)), "manglendeBarnepass"),
                                  Dokument(Fil(byteArrayOf(12)), "barnMedSærligeBehov"),
                                  Dokument(Fil(byteArrayOf(12)), "arbeidskontrakt"),
                                  Dokument(Fil(byteArrayOf(12)), "utdanningstilbud"),
                                  Dokument(Fil(byteArrayOf(12)), "oppsigelseReduksjonDokumentasjon"))
    }

    @Test
    fun `finnFelter finner alle felter i en struktur`() {

        val søknad = søknad()

        val list: List<Felt<*>> = SøknadTreeWalker.finnFelter(søknad)

        val writeValueAsString = objectMapper.writeValueAsString(list)

        @Suppress("LongLine")
        assertThat(writeValueAsString).isEqualTo("""[{"label":"Personalia","verdi":[{"label":"fødselsnummer","verdi":"24117938529"},{"label":"navn","verdi":"hei"},{"label":"statsborgerskap","verdi":"hei"},{"label":"adresse","verdi":[{"label":"gatenavn","verdi":"hei"},{"label":"husnummer","verdi":"1"},{"label":"husbokstav","verdi":"hei"},{"label":"bolignummer","verdi":"hei"},{"label":"postnummer","verdi":"hei"},{"label":"poststedsnavn","verdi":"hei"},{"label":"kommune","verdi":"hei"}]},{"label":"telefonnummer","verdi":"hei"},{"label":"sivilstatus","verdi":"hei"}]},{"label":"Sivilstandsdetaljer","verdi":[{"label":"giftIUtlandet","verdi":"Ja"},{"label":"dokument","verdi":"giftIUtlandetDokumentasjon"},{"label":"separertEllerSkiltIUtlandet","verdi":"Ja"},{"label":"dokument","verdi":"separertEllerSkiltIUtlandetDokumentasjon"},{"label":"søktOmSkilsmisseSeparasjon","verdi":"Ja"},{"label":"søknadsdato","verdi":"2020-01-21"},{"label":"dokument","verdi":"separasjonsbekreftelse"},{"label":"årsakEnslig","verdi":"hei"},{"label":"dokument","verdi":"samlivsbruddsdokumentasjon"},{"label":"samlivsbruddsdato","verdi":"2020-01-21"},{"label":"fraflytningsdato","verdi":"2020-01-21"},{"label":"spesifikasjonAnnet","verdi":"hei"},{"label":"endringSamværsordningDato","verdi":"2020-01-21"}]},{"label":"Medlemskap","verdi":[{"label":"oppholderDuDegINorge","verdi":"Ja"},{"label":"bosattNorgeSisteÅrene","verdi":"Ja"},{"label":"flyktningstatus","verdi":"Ja"},{"label":"dokument","verdi":"flyktningdokumentasjon"}]},{"label":"Bosituasjon","verdi":[{"label":"delerDuBolig","verdi":"hei"},{"label":"samboerdetaljer","verdi":[{"label":"navn","verdi":"hei"},{"label":"fødselsdato","verdi":"2020-01-21"},{"label":"adresse","verdi":[{"label":"gatenavn","verdi":"hei"},{"label":"husnummer","verdi":"1"},{"label":"husbokstav","verdi":"hei"},{"label":"bolignummer","verdi":"hei"},{"label":"postnummer","verdi":"hei"},{"label":"poststedsnavn","verdi":"hei"},{"label":"kommune","verdi":"hei"}]},{"label":"land","verdi":"hei"}]},{"label":"sammenflyttingsdato","verdi":"2020-01-21"}]},{"label":"Sivilstandsplaner","verdi":[{"label":"harPlaner","verdi":"Ja"},{"label":"fraDato","verdi":"2020-01-21"},{"label":"vordendeSamboerEktefelle","verdi":[{"label":"navn","verdi":"hei"},{"label":"fødselsdato","verdi":"2020-01-21"},{"label":"adresse","verdi":[{"label":"gatenavn","verdi":"hei"},{"label":"husnummer","verdi":"1"},{"label":"husbokstav","verdi":"hei"},{"label":"bolignummer","verdi":"hei"},{"label":"postnummer","verdi":"hei"},{"label":"poststedsnavn","verdi":"hei"},{"label":"kommune","verdi":"hei"}]},{"label":"land","verdi":"hei"}]}]},{"label":"KommendeBarn","verdi":[{"label":"navn","verdi":"hei"},{"label":"fnr","verdi":"hei"},{"label":"annenForelder","verdi":[{"label":"kanIkkeOppgiAnnenForelderFar","verdi":"Ja"},{"label":"ikkeOppgittAnnenForelderBegrunnelse","verdi":"hei"},{"label":"bosattNorge","verdi":"Ja"},{"label":"personalia","verdi":[{"label":"navn","verdi":"hei"},{"label":"fødselsdato","verdi":"2020-01-21"},{"label":"adresse","verdi":[{"label":"gatenavn","verdi":"hei"},{"label":"husnummer","verdi":"1"},{"label":"husbokstav","verdi":"hei"},{"label":"bolignummer","verdi":"hei"},{"label":"postnummer","verdi":"hei"},{"label":"poststedsnavn","verdi":"hei"},{"label":"kommune","verdi":"hei"}]},{"label":"land","verdi":"hei"}]}]},{"label":"samvær","verdi":[{"label":"spørsmålAvtaleOmDeltBosted","verdi":"Ja"},{"label":"dokument","verdi":"avtaleOmDeltBosted"},{"label":"skalAnnenForelderHaSamvær","verdi":"hei"},{"label":"harDereSkriftligAvtaleOmSamvær","verdi":"hei"},{"label":"dokument","verdi":"samværsavtale"},{"label":"hvordanPraktiseresSamværet","verdi":"hei"},{"label":"borAnnenForelderISammeHus","verdi":"Ja"},{"label":"harDereTidligereBoddSammen","verdi":"Ja"},{"label":"nårFlyttetDereFraHverandre","verdi":"2020-01-21"},{"label":"dokument","verdi":"erklæringOmSamlivsbrudd"},{"label":"hvorMyeErDuSammenMedAnnenForelder","verdi":"hei"},{"label":"beskrivSamværUtenBarn","verdi":"hei"}]},{"label":"erBarnetFødt","verdi":"Ja"},{"label":"fødselTermindato","verdi":"2020-01-21"},{"label":"skalBarnetBoHosSøker","verdi":"Ja"},{"label":"dokument","verdi":"terminbekreftelse"}]},{"label":"Aktivitet","verdi":[{"label":"hvordanErArbeidssituasjonen","verdi":"A, B, C"}]},{"label":"Situasjon","verdi":[{"label":"gjelderDetteDeg","verdi":"A, B, C"},{"label":"dokument","verdi":"sykdom"},{"label":"dokument","verdi":"barnsSykdom"},{"label":"dokument","verdi":"manglendeBarnepass"},{"label":"dokument","verdi":"barnMedSærligeBehov"},{"label":"dokument","verdi":"arbeidskontrakt"},{"label":"oppstartNyJobb","verdi":"2020-01-21"},{"label":"dokument","verdi":"utdanningstilbud"},{"label":"oppstartUtdanning","verdi":"2020-01-21"},{"label":"sagtOppEllerRedusertStilling","verdi":"hei"},{"label":"oppsigelseReduksjonÅrsak","verdi":"hei"},{"label":"oppsigelseReduksjonTidspunkt","verdi":"2020-01-21"},{"label":"dokument","verdi":"oppsigelseReduksjonDokumentasjon"}]},{"label":"stønadsstart","verdi":[{"label":"måned","verdi":"august"},{"label":"år","verdi":"1"}]}]""")

    }


    private fun søknad() =
            Søknad(Felt("Personalia",
                        Personalia(Felt("fødselsnummer", Fødselsnummer("24117938529")),
                                   stringfelt("navn"),
                                   stringfelt("statsborgerskap"),
                                   adresseFelt(),
                                   stringfelt("telefonnummer"),
                                   stringfelt("sivilstatus"))),
                   Felt("Sivilstandsdetaljer",
                        Sivilstandsdetaljer(booleanfelt("giftIUtlandet"),
                                            dokumentfelt("giftIUtlandetDokumentasjon"),
                                            booleanfelt("separertEllerSkiltIUtlandet"),
                                            dokumentfelt("separertEllerSkiltIUtlandetDokumentasjon"),
                                            booleanfelt("søktOmSkilsmisseSeparasjon"),
                                            datofelt("søknadsdato"),
                                            dokumentfelt("separasjonsbekreftelse"),
                                            stringfelt("årsakEnslig"),
                                            dokumentfelt("samlivsbruddsdokumentasjon"),
                                            datofelt("samlivsbruddsdato"),
                                            datofelt("fraflytningsdato"),
                                            stringfelt("spesifikasjonAnnet"),
                                            datofelt("endringSamværsordningDato"))),
                   Felt("Medlemskap",
                        Medlemskapsdetaljer(booleanfelt("oppholderDuDegINorge"),
                                            booleanfelt("bosattNorgeSisteÅrene"),
                                            null,
                                            booleanfelt("flyktningstatus"),
                                            dokumentfelt("flyktningdokumentasjon"))),
                   Felt("Bosituasjon",
                        Bosituasjon(stringfelt("delerDuBolig"),
                                    Felt("samboerdetaljer", personMinimum()),
                                    datofelt("sammenflyttingsdato"))),
                   Felt("Sivilstandsplaner",
                        Sivilstandsplaner(booleanfelt("harPlaner"),
                                          datofelt("fraDato"),
                                          Felt("vordendeSamboerEktefelle", personMinimum()))),
                   null,
                   Felt("KommendeBarn",
                        listOf(KommendeBarn(stringfelt("navn"),
                                            stringfelt("fnr"),
                                            Felt("annenForelder", Forelder(booleanfelt("kanIkkeOppgiAnnenForelderFar"),
                                                                           stringfelt("ikkeOppgittAnnenForelderBegrunnelse"),
                                                                           booleanfelt("bosattNorge"),
                                                                           Felt("personalia", personMinimum()))),
                                            Felt("samvær",
                                                 Samvær(booleanfelt("spørsmålAvtaleOmDeltBosted"),
                                                        dokumentfelt("avtaleOmDeltBosted"),
                                                        stringfelt("skalAnnenForelderHaSamvær"),
                                                        stringfelt("harDereSkriftligAvtaleOmSamvær"),
                                                        dokumentfelt("samværsavtale"),
                                                        stringfelt("hvordanPraktiseresSamværet"),
                                                        booleanfelt("borAnnenForelderISammeHus"),
                                                        booleanfelt("harDereTidligereBoddSammen"),
                                                        datofelt("nårFlyttetDereFraHverandre"),
                                                        dokumentfelt("erklæringOmSamlivsbrudd"),
                                                        stringfelt("hvorMyeErDuSammenMedAnnenForelder"),
                                                        stringfelt("beskrivSamværUtenBarn"))),
                                            booleanfelt("erBarnetFødt"),
                                            datofelt("fødselTermindato"),
                                            booleanfelt("skalBarnetBoHosSøker"),
                                            dokumentfelt("terminbekreftelse")))),
                   Felt("Aktivitet",
                        Aktivitet(listefelt("hvordanErArbeidssituasjonen"), null, null, null, null, null)),
                   Felt("Situasjon",
                        Situasjon(listefelt("gjelderDetteDeg"),
                                  dokumentfelt("sykdom"),
                                  dokumentfelt("barnsSykdom"),
                                  dokumentfelt("manglendeBarnepass"),
                                  dokumentfelt("barnMedSærligeBehov"),
                                  dokumentfelt("arbeidskontrakt"),
                                  datofelt("oppstartNyJobb"),
                                  dokumentfelt("utdanningstilbud"),
                                  datofelt("oppstartUtdanning"),
                                  stringfelt("sagtOppEllerRedusertStilling"),
                                  stringfelt("oppsigelseReduksjonÅrsak"),
                                  datofelt("oppsigelseReduksjonTidspunkt"),
                                  dokumentfelt("oppsigelseReduksjonDokumentasjon"))),
                   Felt("stønadsstart", Stønadsstart(Felt("måned", Month.AUGUST), integerfelt("år"))))

    private fun personMinimum(): PersonMinimum {
        return PersonMinimum(stringfelt("navn"),
                             null,
                             datofelt("fødselsdato"),
                             adresseFelt(),
                             stringfelt("land"))
    }

    private fun adresseFelt(): Felt<Adresse> {
        return Felt("adresse",
                    Adresse(stringfelt("gatenavn"),
                            integerfelt("husnummer"),
                            stringfelt("husbokstav"),
                            stringfelt("bolignummer"),
                            stringfelt("postnummer"),
                            stringfelt("poststedsnavn"),
                            stringfelt("kommune")))
    }

    private fun dokumentfelt(tittel: String) = Felt("dokument", Dokument(Fil(byteArrayOf(12)), tittel))

    private fun listefelt(tittel: String) = Felt(tittel, listOf("A", "B", "C"))

    private fun booleanfelt(tittel: String) = Felt(tittel, true)

    private fun datofelt(tittel: String) = Felt(tittel, LocalDate.of(2020, 1, 21))

    private fun integerfelt(tittel: String) = Felt(tittel, 1)

    private fun stringfelt(tittel: String) = Felt(tittel, "hei")
}

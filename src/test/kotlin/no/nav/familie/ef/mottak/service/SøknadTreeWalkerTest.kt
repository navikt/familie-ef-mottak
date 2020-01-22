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

        val list: Map<String, Any> = SøknadTreeWalker.mapSøknadsfelterTilMap(søknad)

        val writeValueAsString = objectMapper.writeValueAsString(list)

        @Suppress("LongLine")
        assertThat(writeValueAsString).isEqualTo("""{"label":"søknad","verdiliste":[{"label":"Personalia","verdiliste":[{"label":"fødselsnummer","verdi":"24117938529"},{"label":"navn","verdi":"hei"},{"label":"statsborgerskap","verdi":"hei"},{"label":"adresse","verdi":"Søknadsfelt(label=gatenavn, verdi=hei) Søknadsfelt(label=husnummer, verdi=1) Søknadsfelt(label=husbokstav, verdi=hei)\nSøknadsfelt(label=postnummer, verdi=hei) Søknadsfelt(label=poststedsnavn, verdi=hei)"},{"label":"telefonnummer","verdi":"hei"},{"label":"sivilstatus","verdi":"hei"}]},{"label":"Sivilstandsdetaljer","verdiliste":[{"label":"giftIUtlandet","verdi":"Ja"},{"label":"dokument","verdi":"giftIUtlandetDokumentasjon"},{"label":"separertEllerSkiltIUtlandet","verdi":"Ja"},{"label":"dokument","verdi":"separertEllerSkiltIUtlandetDokumentasjon"},{"label":"søktOmSkilsmisseSeparasjon","verdi":"Ja"},{"label":"søknadsdato","verdi":"2020-01-21"},{"label":"dokument","verdi":"separasjonsbekreftelse"},{"label":"årsakEnslig","verdi":"hei"},{"label":"dokument","verdi":"samlivsbruddsdokumentasjon"},{"label":"samlivsbruddsdato","verdi":"2020-01-21"},{"label":"fraflytningsdato","verdi":"2020-01-21"},{"label":"spesifikasjonAnnet","verdi":"hei"},{"label":"endringSamværsordningDato","verdi":"2020-01-21"}]},{"label":"Medlemskap","verdiliste":[{"label":"oppholderDuDegINorge","verdi":"Ja"},{"label":"bosattNorgeSisteÅrene","verdi":"Ja"},{"label":"flyktningstatus","verdi":"Ja"},{"label":"dokument","verdi":"flyktningdokumentasjon"}]},{"label":"Bosituasjon","verdiliste":[{"label":"delerDuBolig","verdi":"hei"},{"label":"samboerdetaljer","verdiliste":[{"label":"navn","verdi":"hei"},{"label":"fødselsdato","verdi":"2020-01-21"},{"label":"land","verdi":"hei"}]},{"label":"sammenflyttingsdato","verdi":"2020-01-21"}]},{"label":"Sivilstandsplaner","verdiliste":[{"label":"harPlaner","verdi":"Ja"},{"label":"fraDato","verdi":"2020-01-21"},{"label":"vordendeSamboerEktefelle","verdiliste":[{"label":"navn","verdi":"hei"},{"label":"fødselsdato","verdi":"2020-01-21"},{"label":"land","verdi":"hei"}]}]},{"label":"KommendeBarn","verdiliste":[{"label":"navn","verdi":"hei"},{"label":"fnr","verdi":"hei"},{"label":"annenForelder","verdiliste":[{"label":"kanIkkeOppgiAnnenForelderFar","verdi":"Ja"},{"label":"ikkeOppgittAnnenForelderBegrunnelse","verdi":"hei"},{"label":"bosattNorge","verdi":"Ja"},{"label":"personalia","verdiliste":[{"label":"navn","verdi":"hei"},{"label":"fødselsdato","verdi":"2020-01-21"},{"label":"land","verdi":"hei"}]},{"label":"adresse","verdi":"Søknadsfelt(label=gatenavn, verdi=hei) Søknadsfelt(label=husnummer, verdi=1) Søknadsfelt(label=husbokstav, verdi=hei)\nSøknadsfelt(label=postnummer, verdi=hei) Søknadsfelt(label=poststedsnavn, verdi=hei)"}]},{"label":"samvær","verdiliste":[{"label":"spørsmålAvtaleOmDeltBosted","verdi":"Ja"},{"label":"dokument","verdi":"avtaleOmDeltBosted"},{"label":"skalAnnenForelderHaSamvær","verdi":"hei"},{"label":"harDereSkriftligAvtaleOmSamvær","verdi":"hei"},{"label":"dokument","verdi":"samværsavtale"},{"label":"hvordanPraktiseresSamværet","verdi":"hei"},{"label":"borAnnenForelderISammeHus","verdi":"Ja"},{"label":"harDereTidligereBoddSammen","verdi":"Ja"},{"label":"nårFlyttetDereFraHverandre","verdi":"2020-01-21"},{"label":"dokument","verdi":"erklæringOmSamlivsbrudd"},{"label":"hvorMyeErDuSammenMedAnnenForelder","verdi":"hei"},{"label":"beskrivSamværUtenBarn","verdi":"hei"}]},{"label":"erBarnetFødt","verdi":"Ja"},{"label":"fødselTermindato","verdi":"2020-01-21"},{"label":"skalBarnetBoHosSøker","verdi":"Ja"},{"label":"dokument","verdi":"terminbekreftelse"}]},{"label":"Aktivitet","verdiliste":[{"label":"hvordanErArbeidssituasjonen","verdi":"A\nB\nC"}]},{"label":"Situasjon","verdiliste":[{"label":"gjelderDetteDeg","verdi":"A\nB\nC"},{"label":"dokument","verdi":"sykdom"},{"label":"dokument","verdi":"barnsSykdom"},{"label":"dokument","verdi":"manglendeBarnepass"},{"label":"dokument","verdi":"barnMedSærligeBehov"},{"label":"dokument","verdi":"arbeidskontrakt"},{"label":"oppstartNyJobb","verdi":"2020-01-21"},{"label":"dokument","verdi":"utdanningstilbud"},{"label":"oppstartUtdanning","verdi":"2020-01-21"},{"label":"sagtOppEllerRedusertStilling","verdi":"hei"},{"label":"oppsigelseReduksjonÅrsak","verdi":"hei"},{"label":"oppsigelseReduksjonTidspunkt","verdi":"2020-01-21"},{"label":"dokument","verdi":"oppsigelseReduksjonDokumentasjon"}]},{"label":"stønadsstart","verdiliste":[{"label":"måned","verdi":"august"},{"label":"år","verdi":"1"}]}]}""")
    }

    private fun søknad() =
            Søknad(Søknadsfelt("Personalia",
                               Personalia(Søknadsfelt("fødselsnummer", Fødselsnummer("24117938529")),
                                          stringfelt("navn"),
                                          stringfelt("statsborgerskap"),
                                          adresseSøknadsfelt(),
                                          stringfelt("telefonnummer"),
                                          stringfelt("sivilstatus"))),
                   Søknadsfelt("Sivilstandsdetaljer",
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
                   Søknadsfelt("Medlemskap",
                               Medlemskapsdetaljer(booleanfelt("oppholderDuDegINorge"),
                                                   booleanfelt("bosattNorgeSisteÅrene"),
                                                   null,
                                                   booleanfelt("flyktningstatus"),
                                                   dokumentfelt("flyktningdokumentasjon"))),
                   Søknadsfelt("Bosituasjon",
                               Bosituasjon(stringfelt("delerDuBolig"),
                                           Søknadsfelt("samboerdetaljer", personMinimum()),
                                           datofelt("sammenflyttingsdato"))),
                   Søknadsfelt("Sivilstandsplaner",
                               Sivilstandsplaner(booleanfelt("harPlaner"),
                                                 datofelt("fraDato"),
                                                 Søknadsfelt("vordendeSamboerEktefelle", personMinimum()))),
                   null,
                   Søknadsfelt("KommendeBarn",
                               listOf(KommendeBarn(stringfelt("navn"),
                                                   stringfelt("fnr"),
                                                   Søknadsfelt("annenForelder",
                                                               Forelder(booleanfelt("kanIkkeOppgiAnnenForelderFar"),
                                                                        stringfelt("ikkeOppgittAnnenForelderBegrunnelse"),
                                                                        booleanfelt("bosattNorge"),
                                                                        Søknadsfelt("personalia", personMinimum()),
                                                                        adresseSøknadsfelt())),
                                                   Søknadsfelt("samvær",
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
                   Søknadsfelt("Aktivitet",
                               Aktivitet(listefelt("hvordanErArbeidssituasjonen"), null, null, null, null, null)),
                   Søknadsfelt("Situasjon",
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
                   Søknadsfelt("stønadsstart", Stønadsstart(Søknadsfelt("måned", Month.AUGUST), integerfelt("år"))))

    private fun personMinimum(): PersonMinimum {
        return PersonMinimum(stringfelt("navn"),
                             null,
                             datofelt("fødselsdato"),
                             stringfelt("land"))
    }

    private fun adresseSøknadsfelt(): Søknadsfelt<Adresse> {
        return Søknadsfelt("adresse",
                           Adresse(stringfelt("gatenavn"),
                                   integerfelt("husnummer"),
                                   stringfelt("husbokstav"),
                                   stringfelt("bolignummer"),
                                   stringfelt("postnummer"),
                                   stringfelt("poststedsnavn"),
                                   stringfelt("kommune")))
    }

    private fun dokumentfelt(tittel: String) = Søknadsfelt("dokument", Dokument(Fil(byteArrayOf(12)), tittel))

    private fun listefelt(tittel: String) = Søknadsfelt(tittel, listOf("A", "B", "C"))

    private fun booleanfelt(tittel: String) = Søknadsfelt(tittel, true)

    private fun datofelt(tittel: String) = Søknadsfelt(tittel, LocalDate.of(2020, 1, 21))

    private fun integerfelt(tittel: String) = Søknadsfelt(tittel, 1)

    private fun stringfelt(tittel: String) = Søknadsfelt(tittel, "hei")
}

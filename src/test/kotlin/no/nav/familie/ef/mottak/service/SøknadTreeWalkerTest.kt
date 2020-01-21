package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import no.nav.familie.kontrakter.ef.søknad.*
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

        print(list)

    }

    @Test
    fun `finnFelter finner alle felter i en struktur`() {

        val søknad = søknad()

        val list: List<List<Felt<*>>> = SøknadTreeWalker.finnFelter(søknad)


        list.forEach {
            println(it)
            it.forEach { inner -> println(inner) }
        }


    }


    private fun søknad() =
            Søknad(Felt("Personalia",
                        Personalia(Felt("", Fødselsnummer("24117938529")),
                                   fs("navn"),
                                   fs("statsborgerskap"),
                                   adresseFelt(),
                                   fs("telefonnummer"),
                                   fs("sivilstatus"))),
                   Felt("Sivilstandsdetaljer",
                        Sivilstandsdetaljer(fb("giftIUtlandet"),
                                            dokument("giftIUtlandetDokumentasjon"),
                                            fb("separertEllerSkiltIUtlandet"),
                                            dokument("separertEllerSkiltIUtlandetDokumentasjon"),
                                            fb("søktOmSkilsmisseSeparasjon"),
                                            fd("søknadsdato"),
                                            dokument("separasjonsbekreftelse"),
                                            fs("årsakEnslig"),
                                            dokument("samlivsbruddsdokumentasjon"),
                                            fd("samlivsbruddsdato"),
                                            fd("fraflytningsdato"),
                                            fs("spesifikasjonAnnet"),
                                            fd("endringSamværsordningDato"))),
                   Felt("Medlemskap",
                        Medlemskapsdetaljer(fb("oppholderDuDegINorge"),
                                            fb("bosattNorgeSisteÅrene"),
                                            Felt("utenlandsopphold", emptyList()),
                                            fb("flyktningstatus"),
                                            dokument("flyktningdokumentasjon"))),
                   Felt("Bosituasjon",
                        Bosituasjon(fs("delerDuBolig"),
                                    Felt("samboerdetaljer", personMinimum()),
                                    fd("sammenflyttingsdato"))),
                   Felt("Sivilstandsplaner",
                        Sivilstandsplaner(fb("harPlaner"),
                                          fd("fraDato"),
                                          Felt("vordendeSamboerEktefelle", personMinimum()))),
                   null,
                   Felt("KommendeBarn",
                        listOf(KommendeBarn(fs("navn"),
                                            fs("fnr"),
                                            Felt("annenForelder", Forelder(fb("kanIkkeOppgiAnnenForelderFar"),
                                                                           fs("ikkeOppgittAnnenForelderBegrunnelse"),
                                                                           fb("bosattNorge"),
                                                                           Felt("personalia", personMinimum()))),
                                            Felt("samvær",
                                                 Samvær(fb("spørsmålAvtaleOmDeltBosted"),
                                                        dokument("avtaleOmDeltBosted"),
                                                        fs("skalAnnenForelderHaSamvær"),
                                                        fs("harDereSkriftligAvtaleOmSamvær"),
                                                        dokument("samværsavtale"),
                                                        fs("hvordanPraktiseresSamværet"),
                                                        fb("borAnnenForelderISammeHus"),
                                                        fb("harDereTidligereBoddSammen"),
                                                        fd("nårFlyttetDereFraHverandre"),
                                                        dokument("erklæringOmSamlivsbrudd"),
                                                        fs("hvorMyeErDuSammenMedAnnenForelder"),
                                                        fs("beskrivSamværUtenBarn"))),
                                            fb("erBarnetFødt"),
                                            fd("fødselTermindato"),
                                            fb("skalBarnetBoHosSøker"),
                                            dokument("terminbekreftelse")))),
                   Felt("Aktivitet",
                        Aktivitet(fl("hvordanErArbeidssituasjonen"), null, null, null, null, null)),
                   Felt("Situasjon",
                        Situasjon(fl("gjelderDetteDeg"),
                                  dokument("sykdom"),
                                  dokument("barnsSykdom"),
                                  dokument("manglendeBarnepass"),
                                  dokument("barnMedSærligeBehov"),
                                  dokument("arbeidskontrakt"),
                                  fd("oppstartNyJobb"),
                                  dokument("utdanningstilbud"),
                                  fd("oppstartUtdanning"),
                                  fs("sagtOppEllerRedusertStilling"),
                                  fs("oppsigelseReduksjonÅrsak"),
                                  fd("oppsigelseReduksjonTidspunkt"),
                                  dokument("oppsigelseReduksjonDokumentasjon"))),
                   Felt("stønadsstart", Stønadsstart(Felt("måned", Month.AUGUST), fi("år"))))

    private fun personMinimum(): PersonMinimum {
        return PersonMinimum(fs("navn"),
                             null,
                             fd("fødselsdato"),
                             adresseFelt(),
                             fs("land"))
    }

    private fun adresseFelt(): Felt<Adresse> {
        return Felt("adresse",
                    Adresse(fs("gatenavn"),
                            fi("husnummer"),
                            fs("husbokstav"),
                            fs("bolignummer"),
                            fs("postnummer"),
                            fs("poststedsnavn"),
                            fs("kommune")))
    }

    private fun dokument(tittel: String) = Felt("dokument",
                                                Dokument(Fil(byteArrayOf(12)),
                                                         tittel))


    private fun fl(tittel: String) = Felt(tittel, listOf("A", "B", "C"))
    private fun fb(tittel: String) = Felt(tittel, true)
    private fun fd(tittel: String) = Felt(tittel, LocalDate.now())
    private fun fi(tittel: String) = Felt(tittel, 1)
    private fun fs(tittel: String) = Felt(tittel, "hei")


}
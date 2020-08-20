package no.nav.familie.ef.mottak.service

import no.nav.familie.kontrakter.ef.søknad.*
import java.time.LocalDate
import java.time.Month
import java.util.*

internal object Testdata {

    fun randomFnr(): String = UUID.randomUUID().toString()
    fun randomAktørId(): String = UUID.randomUUID().toString()
    private val mottat = LocalDate.of(2020, 1, 1).atStartOfDay()

    val skjemaForArbeidssøker =
            SkjemaForArbeidssøker(Søknadsfelt("Søker", lagPersonaliaForArbeidssøker()),
                                  Søknadsfelt("Arbeidssøker",
                                              Arbeidssøker(Søknadsfelt("Er du registrert som arbeidssøker hos Nav?",
                                                                       true),
                                                           Søknadsfelt("Er du villig til å ta imot tilbud om arbeid?",
                                                                       true),
                                                           Søknadsfelt("Kan du begynne innen en uke?", true),
                                                           Søknadsfelt("Kan du skaffe barnepass innen en uke?",
                                                                       true),
                                                           Søknadsfelt("Hvor ønsker du arbeid?", "Mordor"),

                                                           Søknadsfelt("Ønsker du minst 50 prosent stilling?",
                                                                       true))),
                                  Søknadsfelt("detaljer", Innsendingsdetaljer(Søknadsfelt("mottat", mottat))))

    private fun lagPersonaliaForArbeidssøker(): PersonaliaArbeidssøker {
        return PersonaliaArbeidssøker(Søknadsfelt("fnr", Fødselsnummer("03125462714")), // random fnr anno 1854
                                      Søknadsfelt("Navn", "Navnesen"))
    }

    val søknadOvergangsstønad = SøknadOvergangsstønad(Søknadsfelt("Søker", personalia()),
                                                      Søknadsfelt("detaljer", Innsendingsdetaljer(Søknadsfelt("mottat", mottat))),
                                                      Søknadsfelt("Detaljer om sivilstand", sivilstandsdetaljer()),
                                                      Søknadsfelt("Opphold i Norge", medlemskapsdetaljer()),
                                                      Søknadsfelt("Bosituasjonen din", bosituasjon()),
                                                      Søknadsfelt("Sivilstandsplaner", sivilstandsplaner()),
                                                      Søknadsfelt("Barn", listOf(barn())),
                                                      Søknadsfelt("Arbeid, utdanning og andre aktiviteter", aktivitet()),
                                                      Søknadsfelt("Mer om situasjonen din", situasjon()),
                                                      Søknadsfelt("Når søker du stønad fra?", stønadsstart()))

    val søknadBarnetilsyn = SøknadBarnetilsyn(Søknadsfelt("Søker", personalia()),
                                              Søknadsfelt("detaljer", Innsendingsdetaljer(Søknadsfelt("mottat", mottat))),
                                              Søknadsfelt("Detaljer om sivilstand", sivilstandsdetaljer()),
                                              Søknadsfelt("Opphold i Norge", medlemskapsdetaljer()),
                                              Søknadsfelt("Bosituasjonen din", bosituasjon()),
                                              Søknadsfelt("Sivilstandsplaner", sivilstandsplaner()),
                                              Søknadsfelt("Barn", listOf(barn(barnetilsyn = true))),
                                              Søknadsfelt("Arbeid, utdanning og andre aktiviteter", aktivitet()),
                                              Søknadsfelt("Når søker du stønad fra?", stønadsstart()),
                                              dokumentasjon = BarnetilsynDokumentasjon(
                                                      barnepassordningFaktura = dokumentfelt("Barnepassordning faktura"),
                                                      avtaleBarnepasser = dokumentfelt("Avtale barnepasser"),
                                                      arbeidstid = dokumentfelt("Arbeidstid"),
                                                      spesielleBehov = dokumentfelt("Spesielle behov")))

    val søknadSkolepenger = SøknadSkolepenger(Søknadsfelt("Søker", personalia()),
                                              Søknadsfelt("detaljer", Innsendingsdetaljer(Søknadsfelt("mottat", mottat))),
                                              Søknadsfelt("Detaljer om sivilstand", sivilstandsdetaljer()),
                                              Søknadsfelt("Opphold i Norge", medlemskapsdetaljer()),
                                              Søknadsfelt("Bosituasjonen din", bosituasjon()),
                                              Søknadsfelt("Sivilstandsplaner", sivilstandsplaner()),
                                              Søknadsfelt("Arbeid, utdanning og andre aktiviteter", utdanning()),
                                              dokumentasjon = SkolepengerDokumentasjon())

    private fun utdanning(): UnderUtdanning {
        return UnderUtdanning(Søknadsfelt("Skole/utdanningssted", "UiO"),
                              Søknadsfelt("Utdanning",
                                          Utdanning(Søknadsfelt("Linje/kurs/grad",
                                                                "Profesjonsstudium Informatikk"),
                                                    Søknadsfelt("Når skal du være elev/student?",
                                                                Periode(Month.JANUARY,
                                                                        1999,
                                                                        Month.OCTOBER,
                                                                        2004))
                                          )),
                              Søknadsfelt("Er utdanningen offentlig eller privat?",
                                          "Offentlig"),
                              Søknadsfelt("Heltid, eller deltid", "Deltid"),
                              Søknadsfelt("Hvor mye skal du studere?", 300),
                              Søknadsfelt("Hva er målet med utdanningen?",
                                          "Økonomisk selvstendighet"),
                              Søknadsfelt("Har du tatt utdanning etter grunnskolen?", true),
                              Søknadsfelt("Tidligere Utdanning",
                                          listOf(Utdanning(Søknadsfelt("Linje/kurs/grad",
                                                                       "Master Fysikk"),
                                                           Søknadsfelt("Når var du elev/student?",
                                                                       Periode(Month.JANUARY,
                                                                               1999,
                                                                               Month.OCTOBER,
                                                                               2004))))))
    }




    private const val vedleggId = "d5531f89-0079-4715-a337-9fd28f811f2f"

    val vedlegg = listOf(Vedlegg(vedleggId, "navn.pdf", "Dokumentasjon på at du er syk"))

    private fun stønadsstart() = Stønadsstart(Søknadsfelt("Fra måned", Month.AUGUST),
                                              Søknadsfelt("Fra år", 2018),
                                              Søknadsfelt("Søke fra bestemt mnd", true))

    @Suppress("LongLine")
    private fun situasjon(): Situasjon {
        return Situasjon(Søknadsfelt("Gjelder noe av dette deg?",
                                     listOf("Barnet mitt er sykt",
                                            "Jeg har søkt om barnepass, men ikke fått plass enda",
                                            "Jeg har barn som har behov for særlig tilsyn på grunn av fysiske, psykiske eller store sosiale problemer")),
                         dokumentfelt("Legeerklæring"),
                         dokumentfelt("Legeattest for egen sykdom eller sykt barn"),
                         dokumentfelt("Avslag på søknad om barnehageplass, skolefritidsordning e.l."),
                         dokumentfelt("Dokumentasjon av særlig tilsynsbehov"),
                         dokumentfelt("Dokumentasjon av studieopptak"),
                         dokumentfelt("Læringskontrakt"),
                         Søknadsfelt("Når skal du starte i ny jobb?", LocalDate.of(2045, 12, 16)),
                         dokumentfelt("Dokumentasjon av jobbtilbud"),
                         Søknadsfelt("Når skal du starte utdanningen?", LocalDate.of(2025, 7, 28)),
                         Søknadsfelt("Har du sagt opp jobben eller redusert arbeidstiden de siste 6 månedene?",
                                     "Ja, jeg har sagt opp jobben eller tatt frivillig permisjon (ikke foreldrepermisjon)"),
                         Søknadsfelt("Hvorfor sa du opp?", "Sjefen var dum"),
                         Søknadsfelt("Når sa du opp?", LocalDate.of(2014, 1, 12)),
                         dokumentfelt("Dokumentasjon av arbeidsforhold"))
    }

    @Suppress("LongLine")
    private fun aktivitet(): Aktivitet {
        return Aktivitet(Søknadsfelt("Hvordan er arbeidssituasjonen din?",
                                     listOf("Jeg er hjemme med barn under 1 år",
                                            "Jeg er i arbeid",
                                            "Jeg er selvstendig næringsdrivende eller frilanser")),
                         Søknadsfelt("Om arbeidsforholdet ditt",
                                     listOf(Arbeidsgiver(Søknadsfelt("Navn på arbeidsgiveren", "Palpatine"),
                                                         Søknadsfelt("Hvor mye jobber du?", 15),
                                                         Søknadsfelt("Er stillingen fast eller midlertidig?",
                                                                     "Fast"),
                                                         Søknadsfelt("Har du en sluttdato?", true),
                                                         Søknadsfelt("Når skal du slutte?",
                                                                     LocalDate.of(2020, 11, 18))))),
                         null,
                         Søknadsfelt("Om firmaet du driver",
                                     listOf(Selvstendig(Søknadsfelt("Navn på firma", "Bobs burgers"),
                                                 Søknadsfelt("Organisasjonsnummer", "987654321"),
                                                 Søknadsfelt("Når etablerte du firmaet?",
                                                             LocalDate.of(2018, 4, 5)),
                                                 Søknadsfelt("Hvor mye jobber du?", 150),
                                                 Søknadsfelt("Hvordan ser arbeidsuken din ut?",
                                                             "Veldig tung")))),
                         Søknadsfelt("Om virksomheten du etablerer",
                                     Virksomhet(Søknadsfelt("Beskriv virksomheten",
                                                            "Den kommer til å revolusjonere verden"))),
                         Søknadsfelt("Når du er arbeidssøker",
                                     Arbeidssøker(registrertSomArbeidssøkerNav = Søknadsfelt("Er du registrert som arbeidssøker hos NAV?",
                                                                                             true),
                                                  villigTilÅTaImotTilbudOmArbeid = Søknadsfelt("Er du villig til å ta imot tilbud om arbeid eller arbeidsmarkedstiltak?",
                                                                                               true),
                                                  kanDuBegynneInnenEnUke = Søknadsfelt("Kan du begynne i arbeid senest én uke etter at du har fått tilbud om jobb?",
                                                                                       true),
                                                  kanDuSkaffeBarnepassInnenEnUke = Søknadsfelt("Har du eller kan du skaffe barnepass senest innen en uke etter at du har fått tilbud om jobb eller arbeidsmarkedstiltak?",
                                                                                               false),
                                                  hvorØnskerDuArbeid = Søknadsfelt("Hvor ønsker du å søke arbeid?",
                                                                                   "Kun i bodistriktet mitt, ikke mer enn 1 times reisevei"),
                                                  ønskerDuMinst50ProsentStilling = Søknadsfelt("Ønsker du å stå som arbeidssøker til minst 50% stilling?",
                                                                                               true),
                                                  ikkeVilligTilÅTaImotTilbudOmArbeidDokumentasjon = dokumentfelt("Dokumentasjon - ikke villig til å ta import tilbud om arbeid")
                                     )

                         ),
                         Søknadsfelt("Utdanningen du skal ta",
                                     UnderUtdanning(Søknadsfelt("Skole/utdanningssted", "UiO"),
                                                    Søknadsfelt("Utdanning",
                                                                Utdanning(Søknadsfelt("Linje/kurs/grad",
                                                                                      "Profesjonsstudium Informatikk"),
                                                                          Søknadsfelt("Når skal du være elev/student?",
                                                                                      Periode(Month.JANUARY,
                                                                                              1999,
                                                                                              Month.OCTOBER,
                                                                                              2004))
                                                                )),
                                                    Søknadsfelt("Er utdanningen offentlig eller privat?",
                                                                "Offentlig"),
                                                    Søknadsfelt("Heltid, eller deltid", "Deltid"),
                                                    Søknadsfelt("Hvor mye skal du studere?", 300),
                                                    Søknadsfelt("Hva er målet med utdanningen?",
                                                                "Økonomisk selvstendighet"),
                                                    Søknadsfelt("Har du tatt utdanning etter grunnskolen?", true),
                                                    Søknadsfelt("Tidligere Utdanning",
                                                                listOf(Utdanning(Søknadsfelt("Linje/kurs/grad",
                                                                                             "Master Fysikk"),
                                                                                 Søknadsfelt("Når var du elev/student?",
                                                                                             Periode(Month.JANUARY,
                                                                                                     1999,
                                                                                                     Month.OCTOBER,
                                                                                                     2004))
                                                                ))))))
    }

    @Suppress("LongLine")
    private fun barn(barnetilsyn: Boolean = false): Barn {
        return Barn(navn = Søknadsfelt("Barnets fulle navn, hvis dette er bestemt", "Sorgløs"),
                    erBarnetFødt = Søknadsfelt("Er barnet født?", false),
                    fødselTermindato = Søknadsfelt("Termindato", LocalDate.of(2020, 5, 16)),
                    terminbekreftelse = dokumentfelt("Bekreftelse på ventet fødselsdato"),
                    annenForelder = Søknadsfelt("Barnets andre forelder",
                                                AnnenForelder(Søknadsfelt("Hvorfor kan du ikke oppgi den andre forelderen?",
                                                                          "Fordi jeg ikke liker hen."))),
                    fødselsnummer = Søknadsfelt("Fødselsnummer", Fødselsnummer("03125462714")), // random fnr anno 1854,
                    harSkalHaSammeAdresse = Søknadsfelt("Skal ha samme adresse", true),
                    ikkeRegistrertPåSøkersAdresseBeskrivelse = Søknadsfelt("Ikke registrert på søkers adresse", "Nei"),
                    samvær = Søknadsfelt("Samvær",
                                         Samvær(Søknadsfelt("Har du og den andre forelderen skriftlig avtale om delt bosted for barnet?",
                                                            true),
                                                dokumentfelt("Avtale om delt bosted for barna"),
                                                Søknadsfelt("Har den andre forelderen samvær med barnet",
                                                            "Ja, men ikke mer enn vanlig samværsrett"),
                                                Søknadsfelt("Har dere skriftlig samværsavtale for barnet?",
                                                            "Ja, men den beskriver ikke når barnet er sammen med hver av foreldrene"),
                                                dokumentfelt("Avtale om samvær"),
                                                dokumentfelt("Skal barnet bo hos deg"),
                                                Søknadsfelt("Hvordan praktiserer dere samværet?",
                                                            "Litt hver for oss"),
                                                Søknadsfelt("Bor du og den andre forelderen til [barnets navn] i samme hus/blokk, gårdstun, kvartal eller vei?",
                                                            "ja"),
                                                Søknadsfelt("Bor du og den andre forelderen til  i samme hus/blokk beskrivelse",
                                                            "Ekstra info?"),
                                                Søknadsfelt("Har du bodd sammen med den andre forelderen til [barnets fornavn] før?",
                                                            true),
                                                Søknadsfelt("Når flyttet dere fra hverandre?",
                                                            LocalDate.of(2018, 7, 21)),
                                                dokumentfelt("Erklæring om samlivsbrudd"),
                                                Søknadsfelt("Hvor mye er du sammen med den andre forelderen til barnet?",
                                                            "Vi møtes også uten at barnet er til stede"),
                                                Søknadsfelt("Beskriv  hvor mye er du sammen med den andre forelderen til barnet?",
                                                            "Vi sees stadig vekk"))),
                    skalHaBarnepass = if (barnetilsyn) Søknadsfelt("Skal ha barnepass", true) else null,
                    barnepass = if (barnetilsyn) barnepass() else null)
    }

    private fun barnepass(): Søknadsfelt<Barnepass> {
        return Søknadsfelt("Barnepass", Barnepass(
                årsakBarnepass = Søknadsfelt("Årsak Barnepass", "Årsak"),
                barnepassordninger = Søknadsfelt("Ordninger", listOf(BarnepassOrdning(
                        hvaSlagsBarnepassOrdning = Søknadsfelt("Hva slags barnepassordning?", "En"),
                        navn = Søknadsfelt("Navn", "navn"),
                        periode = Søknadsfelt("Periode", Periode(Month.JANUARY, 2020, Month.JULY, 2020)),
                        belop = Søknadsfelt("Beløp", 1000.213))
                ))))
    }

    private fun sivilstandsplaner(): Sivilstandsplaner {
        return Sivilstandsplaner(Søknadsfelt("Har du konkrete planer om å gifte deg eller bli samboer", true),
                                 Søknadsfelt("Når skal dette skje?", LocalDate.of(2021, 4, 15)),
                                 Søknadsfelt("Hvem skal du gifte deg eller bli samboer med?", personMinimum()))
    }

    private fun bosituasjon(): Bosituasjon {
        return Bosituasjon(Søknadsfelt("Deler du bolig med andre voksne?",
                                       "Ja, jeg har samboer og lever i et ekteskapslignende forhold"),
                           Søknadsfelt("Om samboeren din", personMinimum()),
                           Søknadsfelt("Når flyttet dere sammen?", LocalDate.of(2018, 8, 12)))
    }

    private fun medlemskapsdetaljer(): Medlemskapsdetaljer {
        return Medlemskapsdetaljer(Søknadsfelt("Oppholder du deg i Norge?", true),
                                   Søknadsfelt("Har du bodd i Norge de siste tre årene?", true),
                                   Søknadsfelt("Utenlandsopphold",
                                               listOf(Utenlandsopphold(Søknadsfelt("Fra",
                                                                                   LocalDate.of(2012, 12, 4)),
                                                                       Søknadsfelt("Til",
                                                                                   LocalDate.of(2012, 12, 18)),
                                                                       Søknadsfelt("Hvorfor bodde du i utlandet?",
                                                                                   "Granca, Granca, Granca")))))
    }

    @Suppress("LongLine")
    private fun sivilstandsdetaljer(): Sivilstandsdetaljer {
        return Sivilstandsdetaljer(Søknadsfelt("Er du gift uten at dette er formelt registrert eller godkjent i Norge?",
                                               true),
                                   dokumentfelt("giftIUtlandetDokumentasjon"),
                                   Søknadsfelt("Er du separert eller skilt uten at dette er formelt registrert eller godkjent i Norge?",
                                               true),
                                   dokumentfelt("separertEllerSkiltIUtlandetDokumentasjon"),
                                   Søknadsfelt("Har dere søkt om separasjon, søkt om skilsmisse eller reist sak for domstolen?",
                                               true),
                                   Søknadsfelt("Når søkte dere eller reiste sak?", LocalDate.of(2015, 12, 23)),
                                   dokumentfelt("Skilsmisse- eller separasjonsbevilling"),
                                   Søknadsfelt("Hva er grunnen til at du er alene med barn?",
                                               "Endring i samværsordning"),
                                   dokumentfelt("Erklæring om samlivsbrudd"),
                                   Søknadsfelt("Dato for samlivsbrudd", LocalDate.of(2014, 10, 3)),
                                   Søknadsfelt("Når flyttet dere fra hverandre?", LocalDate.of(2014, 10, 4)),
                                   Søknadsfelt("Når skjedde endringen / når skal endringen skje?",
                                               LocalDate.of(2013, 4, 17)))
    }

    private fun personalia(): Personalia {
        return Personalia(Søknadsfelt("Fødselsnummer", Fødselsnummer("03125462714")), // random fnr anno 1854
                          Søknadsfelt("Navn", "Kari Nordmann"),
                          Søknadsfelt("Statsborgerskap", "Norsk"),
                          adresseSøknadsfelt(),
                          Søknadsfelt("Telefonnummer", "12345678"),
                          Søknadsfelt("Sivilstand", "Ugift"))
    }

    private fun adresseSøknadsfelt(): Søknadsfelt<Adresse> {
        return Søknadsfelt("Adresse",
                           Adresse("Jerpefaret 5C",
                                   "1440",
                                   "Drøbak",
                                   "Norge"))
    }

    private fun dokumentfelt(tittel: String) =
            Søknadsfelt(tittel, Dokumentasjon(Søknadsfelt("harSendtInn", false), listOf(Dokument(vedleggId, tittel))))

    private fun personMinimum(): PersonMinimum {
        return PersonMinimum(Søknadsfelt("Navn", "Bob Burger"),
                             null,
                             Søknadsfelt("Fødselsdato", LocalDate.of(1992, 2, 18)))
    }
}

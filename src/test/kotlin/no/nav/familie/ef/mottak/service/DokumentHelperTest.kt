package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.service.DokumentHelper
import no.nav.familie.kontrakter.ef.søknad.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month

class DokumentHelperTest {

    @Test
    fun `finnDokumenter finner alle dokumenter i en struktur`() {

        val søknad = søknad()

        val list: List<Dokument> = DokumentHelper.finnDokumenter(søknad)

        assertThat(list).contains(Dokument(Fil(byteArrayOf(12)), "a"),
                                  Dokument(Fil(byteArrayOf(12)), "b"),
                                  Dokument(Fil(byteArrayOf(12)), "c"),
                                  Dokument(Fil(byteArrayOf(12)), "d"),
                                  Dokument(Fil(byteArrayOf(12)), "e"),
                                  Dokument(Fil(byteArrayOf(12)), "f"),
                                  Dokument(Fil(byteArrayOf(12)), "g"),
                                  Dokument(Fil(byteArrayOf(12)), "h"),
                                  Dokument(Fil(byteArrayOf(12)), "i"),
                                  Dokument(Fil(byteArrayOf(12)), "j"),
                                  Dokument(Fil(byteArrayOf(12)), "k"),
                                  Dokument(Fil(byteArrayOf(12)), "l"),
                                  Dokument(Fil(byteArrayOf(12)), "m"),
                                  Dokument(Fil(byteArrayOf(12)), "n"),
                                  Dokument(Fil(byteArrayOf(12)), "o"))

    }


    private fun søknad() =
            Søknad(Felt("", Personalia(Felt("", Fødselsnummer("24117938529")), fs, fs,
                                       Felt("", Adresse(fs, fi, fs, fs, fs, fs, fs)), fs, fs)),
                   Felt("", Sivilstandsdetaljer(fb,
                                                dokument("a"),
                                                fb,
                                                dokument("b"),
                                                fb,
                                                fd,
                                                dokument("c"),
                                                fs,
                                                dokument("d"),
                                                fd,
                                                fd,
                                                fs)),
                   Felt("", Medlemskapsdetaljer(fb, fb, null, fb, dokument("e"))),
                   Felt("", Bosituasjon(fs, null, fd)),
                   Felt("", Sivilstandsplaner(fb, fd, null)),
                   null,
                   Felt("", listOf(KommendeBarn(fs,
                                                fs,
                                                null,
                                                f(Samvær(fb,
                                                       dokument("f"),
                                                       fs,
                                                       fs,
                                                       dokument("g"),
                                                       fs,
                                                       fb,
                                                       fb,
                                                       fd,
                                                       dokument("h"),
                                                       fs,
                                                       fs)),
                                                fb,
                                                fd,
                                                fb,
                                                dokument("j")))),
                   Felt("", Aktivitet(fl, null, null, null, null, null)),
                   Felt("", Situasjon(fl,
                                      dokument("i"),
                                      dokument("j"),
                                      dokument("k"),
                                      dokument("l"),
                                      dokument("m"),
                                      fd,
                                      dokument("n"),
                                      fd,
                                      fs,
                                      fs,
                                      fd,
                                      dokument("o"))),
                   Felt("", Stønadsstart(f(Month.AUGUST), fi)))

    private fun dokument(tittel: String) = Felt("", Dokument(Fil(byteArrayOf(12)), tittel))

    private val date = LocalDate.now()

    private val fl = Felt("", listOf(""))

    private val fb = Felt("", true)


    private val fd = Felt("", LocalDate.now())
    private val fi = Felt("", 1)

    private val fs = Felt("", "")


    fun <T> f(s: T) = Felt("", s)


}
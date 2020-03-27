package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.service.SøknadTreeWalker
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SøknadTreeWalkerTest {

    @Test
    fun `finnDokumenter finner alle dokumenter i en struktur`() {

        val søknad = Testsøknad.søknad

        val list = SøknadTreeWalker.finnDokumenter(søknad).map { it.toString() }

        assertThat(list).contains("giftIUtlandetDokumentasjon",
                                  "separertEllerSkiltIUtlandetDokumentasjon",
                                  "Skilsmisse- eller separasjonsbevilling",
                                  "Avtale om delt bosted for barna",
                                  "Avtale om samvær",
                                  "Erklæring om samlivsbrudd",
                                  "Bekreftelse på ventet fødselsdato",
                                  "Legeerklæring",
                                  "Legeattest for egen sykdom eller sykt barn",
                                  "Avslag på søknad om barnehageplass, skolefritidsordning e.l.",
                                  "Dokumentasjon av særlig tilsynsbehov",
                                  "Dokumentasjon av jobbtilbud",
                                  "Dokumentasjon av studieopptak",
                                  "Dokumentasjon av arbeidsforhold")
    }
}

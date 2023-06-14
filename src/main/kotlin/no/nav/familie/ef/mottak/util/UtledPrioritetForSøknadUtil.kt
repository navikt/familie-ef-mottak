package no.nav.familie.ef.mottak.util

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import java.time.LocalDate

object UtledPrioritetForSøknadUtil {

    fun utledPrioritet(søknad: Søknad): OppgavePrioritet {
        if (skalSetteHøyPriorietSommertid(søknad)) {
            return OppgavePrioritet.HOY
        }
        return OppgavePrioritet.NORM
    }

    private fun skalSetteHøyPriorietSommertid(søknad: Søknad): Boolean {
        if (søknad.dokumenttype != DOKUMENTTYPE_OVERGANGSSTØNAD) {
            return false
        }
        val søknadsdata = objectMapper.readValue<SøknadOvergangsstønad>(søknad.søknadJson.data)
        return (søknadsdata.aktivitet.verdi.underUtdanning != null) && erSommerPeriode(søknad.opprettetTid.toLocalDate())
    }

    private fun erSommerPeriode(opprettetTid: LocalDate): Boolean {
        /**
         * TODO : Sett til start måned til juni før dette merges
         */
        val start = LocalDate.now().withMonth(5).withDayOfMonth(26)
        val end = LocalDate.now().withMonth(9).withDayOfMonth(16)
        return opprettetTid in start..end
    }
}

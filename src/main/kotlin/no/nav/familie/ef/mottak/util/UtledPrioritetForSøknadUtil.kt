package no.nav.familie.ef.mottak.util

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.felles.jsonMapper
import no.nav.familie.kontrakter.felles.oppgave.OppgavePrioritet
import tools.jackson.module.kotlin.readValue
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
        val søknadsdata = jsonMapper.readValue<SøknadOvergangsstønad>(søknad.json)
        return (søknadsdata.aktivitet.verdi.underUtdanning != null) && erSommerPeriode(søknad.opprettetTid.toLocalDate())
    }

    private fun erSommerPeriode(opprettetTid: LocalDate): Boolean {
        val start = LocalDate.now().withMonth(6).withDayOfMonth(26)
        val end = LocalDate.now().withMonth(9).withDayOfMonth(16)
        return opprettetTid in start..end
    }
}

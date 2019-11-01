package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository

interface SøknadRepository : CrudRepository<Søknad, Long> {

    /*
    @Query("""SELECT * FROM Henvendelse
                     WHERE status IN ('KLAR_TIL_PLUKK', 'UBEHANDLET')
                     ORDER BY opprettet_Tidspunkt """)
    fun finnAlleHenvendelserKlareForProsessering(): List<Henvendelse>
     */
}


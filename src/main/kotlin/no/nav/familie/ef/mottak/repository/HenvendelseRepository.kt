package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Henvendelse
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface HenvendelseRepository : CrudRepository<Henvendelse, Long>{

    /*
    @Query("""SELECT * FROM Henvendelse
                     WHERE status IN ('KLAR_TIL_PLUKK', 'UBEHANDLET')
                     ORDER BY opprettet_Tidspunkt """)
    fun finnAlleHenvendelserKlareForProsessering(): List<Henvendelse>
     */
}


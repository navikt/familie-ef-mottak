package no.nav.familie.ef.mottak.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.YearMonth

@Repository
class StatistikkRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    fun antallSøknaderPerDokumentType(): List<SøknaderPerDokumenttype> {
        // language=PostgreSQL
        val sql = """
        SELECT TO_CHAR(opprettet_tid, 'YYYY-MM') dato, dokumenttype, COUNT(*) antall
          FROM soknad
        GROUP BY TO_CHAR(opprettet_tid, 'YYYY-MM'), dokumenttype
        """
        return jdbcTemplate.query(sql) { rs, _ ->
            SøknaderPerDokumenttype(YearMonth.parse(rs.getString("dato")),
                                    rs.getString("dokumenttype"),
                                    rs.getInt("antall"))
        }
    }
}

data class SøknaderPerDokumenttype(val dato: YearMonth, val type: String, val antall: Int)
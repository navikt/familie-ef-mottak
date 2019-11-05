package no.nav.familie.ef.mottak.repository
import no.nav.familie.ef.mottak.api.SøknadController
import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.util.UUID
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.jdbc.support.KeyHolder





@Service
@Transactional
@Repository
class SøknadDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagreSøknad(
            soknad_json: String,
            fnr: String
    ): Long {

        val keyHolder = GeneratedKeyHolder()
        namedParameterJdbcTemplate.update(
                "INSERT INTO SOKNAD (ID, SOKNAD_JSON, FNR) VALUES (nextval('SOKNAD_SEQ'), :soknad_json, :fnr)",
                MapSqlParameterSource()
                        .addValue("soknad_json", soknad_json)
                        .addValue("fnr", fnr),
                keyHolder
        )
        return keyHolder.keys["ID"] as Long
    }


    fun hentSøknadForBruker(id: Long): Søknad? {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM soknad WHERE id = :id",
                MapSqlParameterSource()
                        .addValue("id", id),
                søknadRowMapper
        ).firstOrNull()
    }

    fun slettSøknadForBruker(id: Long) {
        namedParameterJdbcTemplate.update(
                "DELETE FROM soknad WHERE id = :id",

                MapSqlParameterSource().addValue("id", id)
        )
    }
}

val søknadRowMapper: (ResultSet, Int) -> Søknad = { resultSet, _ ->
    Søknad(
            id = resultSet.getLong("id"),
            soknad_json = resultSet.getString("soknad_json")
    )
}



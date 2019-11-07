package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Søknad
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import org.springframework.jdbc.support.GeneratedKeyHolder

@Service
@Repository
@Transactional
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


    fun hentSøknadForBruker(søknadId: Long): Søknad? {
        return namedParameterJdbcTemplate.query(
                "SELECT * FROM SOKNAD WHERE ID = :id",
                MapSqlParameterSource()
                        .addValue("id", søknadId),
                søknadRowMapper
        ).firstOrNull()
    }

    fun slettSøknadForBruker(søknadId: Long) {
        namedParameterJdbcTemplate.update(
                "DELETE FROM SOKNAD WHERE ID = :id",
                MapSqlParameterSource().addValue("id", søknadId)
        )
    }
}

val søknadRowMapper: (ResultSet, Int) -> Søknad = { resultSet, _ ->
    Søknad(
            id = resultSet.getLong("ID"),
            soknad_json = resultSet.getString("SOKNAD_JSON")
    )
}



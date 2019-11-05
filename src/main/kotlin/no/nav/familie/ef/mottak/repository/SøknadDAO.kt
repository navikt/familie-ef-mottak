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

@Service
@Transactional
@Repository
class SøknadDAO(private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate) {

    fun lagreSøknad(
            id: Long,
            soknad_json: String
    ): String {
        val uuid = UUID.randomUUID().toString()

        try {
            namedParameterJdbcTemplate.update(
                    "INSERT INTO soknad (id, soknad_json) VALUES (:id, :soknad_json)",
                    MapSqlParameterSource()
                            .addValue("id", id)
                            .addValue("soknad_json", soknad_json)
            )
        } catch (e: DuplicateKeyException) {

        }
        return "ok, søknad lagra"
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



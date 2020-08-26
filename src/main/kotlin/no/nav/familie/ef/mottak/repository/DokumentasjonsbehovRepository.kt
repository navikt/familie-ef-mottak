package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DokumentasjonsbehovRepository : JpaRepository<Dokumentasjonsbehov, UUID> {

    fun findBySøknadId(søknadId: UUID): Dokumentasjonsbehov?
}

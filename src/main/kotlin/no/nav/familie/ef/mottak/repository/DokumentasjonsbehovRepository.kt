package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DokumentasjonsbehovRepository : JpaRepository<Dokumentasjonsbehov, String>

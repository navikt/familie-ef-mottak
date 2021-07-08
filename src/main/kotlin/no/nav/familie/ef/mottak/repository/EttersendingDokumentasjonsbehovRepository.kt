package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.ef.mottak.repository.domain.EttersendingDokumentasjonsbehov
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EttersendingDokumentasjonsbehovRepository : JpaRepository<EttersendingDokumentasjonsbehov, String>
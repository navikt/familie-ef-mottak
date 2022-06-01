package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.ef.mottak.repository.util.InsertUpdateRepository
import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import org.springframework.stereotype.Repository

@Repository
interface DokumentasjonsbehovRepository :
    RepositoryInterface<Dokumentasjonsbehov, String>,
    InsertUpdateRepository<Dokumentasjonsbehov>

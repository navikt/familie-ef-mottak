package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SøknadskvitteringService(
    private val søknadRepository: SøknadRepository,
) {
    fun hentSøknadskvittering(id: String): Søknad = søknadRepository.findByIdOrThrow(id)
}

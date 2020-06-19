package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.mapper.SakMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.kontrakter.ef.sak.SakRequest
import no.nav.familie.kontrakter.ef.sak.Skjemasak
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SøknadServiceImpl(private val soknadRepository: SoknadRepository,
                        private val søknadClient: SøknadClient) : SøknadService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun motta(søknad: SøknadMedVedlegg): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad)
        val lagretSkjema = soknadRepository.save(søknadDb)
        return Kvittering(lagretSkjema.id, "Søknad lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }

    override fun get(id: String): Soknad {
        return soknadRepository.findByIdOrNull(id) ?: error("Ugyldig primærnøkkel")
    }

    override fun sendTilSak(søknadId: String) {
        val soknad: Soknad = soknadRepository.findByIdOrNull(søknadId) ?: error("")

        if (soknad.dokumenttype == DOKUMENTTYPE_OVERGANGSSTØNAD) {
            val sak: SakRequest = SakMapper.toSak(soknad)
            søknadClient.sendTilSak(sak)
        } else {
            val skjemasak: Skjemasak = SakMapper.toSkjemasak(soknad)
            søknadClient.sendTilSak(skjemasak)
        }
    }

    @Transactional
    override fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering {
        val søknadDb = SøknadMapper.fromDto(skjemaForArbeidssøker)
        val lagretSkjema = soknadRepository.save(søknadDb)
        logger.info("Mottatt skjema med id ${lagretSkjema.id}")

        return Kvittering(søknadDb.id, "Skjema er mottatt og lagret med id ${lagretSkjema.id}.")
    }

    override fun lagreSøknad(soknad: Soknad) {
        soknadRepository.save(soknad)
    }
}

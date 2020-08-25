package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.integration.SøknadClient
import no.nav.familie.ef.mottak.mapper.SakMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.ef.sak.SakRequest
import no.nav.familie.kontrakter.ef.sak.Skjemasak
import no.nav.familie.kontrakter.ef.søknad.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import no.nav.familie.kontrakter.ef.søknad.Vedlegg as VedleggKontrakt

@Service
class SøknadServiceImpl(private val soknadRepository: SoknadRepository,
                        private val vedleggRepository: VedleggRepository,
                        private val dokumentasjonsbehovRepository: DokumentasjonsbehovRepository,
                        private val søknadClient: SøknadClient) : SøknadService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun mottaOvergangsstønad(søknad: SøknadMedVedlegg<SøknadOvergangsstønad>,
                                      vedlegg: Map<String, ByteArray>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad)
        val vedlegg = mapVedlegg(søknadDb.id, søknad.vedlegg, vedlegg)
        return motta(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    override fun mottaBarnetilsyn(søknad: SøknadMedVedlegg<SøknadBarnetilsyn>, vedlegg: Map<String, ByteArray>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad)
        val vedlegg = mapVedlegg(søknadDb.id, søknad.vedlegg, vedlegg)
        return motta(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    override fun mottaSkolepenger(søknad: SøknadMedVedlegg<SøknadSkolepenger>, vedlegg: Map<String, ByteArray>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad)
        val vedlegg = mapVedlegg(søknadDb.id, søknad.vedlegg, vedlegg)
        return motta(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    private fun motta(søknadDb: Soknad,
                      vedlegg: List<Vedlegg>,
                      dokumentasjonsbehov: List<Dokumentasjonsbehov>): Kvittering {
        val lagretSkjema = soknadRepository.save(søknadDb)
        vedleggRepository.saveAll(vedlegg)
        logger.info("Mottatt søknad med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Søknad lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }

    private fun mapVedlegg(søknadDbId: String,
                           vedleggMetadata: List<VedleggKontrakt>,
                           vedlegg: Map<String, ByteArray>): List<Vedlegg> =
            vedleggMetadata.map {
                Vedlegg(id = UUID.fromString(it.id),
                        søknadId = søknadDbId,
                        navn = it.navn,
                        tittel = it.tittel,
                        innhold = Fil(vedlegg[it.id] ?: error("Finner ikke vedlegg med id=${it.id}")))
            }

    override fun get(id: String): Soknad {
        return soknadRepository.findByIdOrNull(id) ?: error("Ugyldig primærnøkkel")
    }

    override fun sendTilSak(søknadId: String) {
        val soknad: Soknad = soknadRepository.findByIdOrNull(søknadId) ?: error("")

        when (soknad.dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> {
                val vedlegg = vedleggRepository.findBySøknadId(søknadId)
                val kontraktVedlegg = vedlegg
                        .map { VedleggKontrakt(it.id.toString(), it.navn, it.tittel) }
                val sak: SakRequest<SøknadOvergangsstønad> = SakMapper.toOvergangsstønadSak(soknad, kontraktVedlegg)
                søknadClient.sendOvergangsstønad(sak, vedlegg.map { it.id.toString() to it.innhold.bytes }.toMap())
            }
            DOKUMENTTYPE_BARNETILSYN -> {
                val vedlegg = vedleggRepository.findBySøknadId(søknadId)
                val kontraktVedlegg = vedlegg
                        .map { VedleggKontrakt(it.id.toString(), it.navn, it.tittel) }
                val sak: SakRequest<SøknadBarnetilsyn> = SakMapper.toBarnetilsynSak(soknad, kontraktVedlegg)
                søknadClient.sendBarnetilsyn(sak, vedlegg.map { it.id.toString() to it.innhold.bytes }.toMap())
            }
            else -> {
                val skjemasak: Skjemasak = SakMapper.toSkjemasak(soknad)
                søknadClient.send(skjemasak)
            }
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

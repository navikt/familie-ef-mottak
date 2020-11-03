package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.ApiFeil
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Soknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov as DatabaseDokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.Vedlegg as VedleggKontrakt

@Service
class SøknadServiceImpl(private val soknadRepository: SoknadRepository,
                        private val vedleggRepository: VedleggRepository,
                        private val dokumentasjonsbehovRepository: DokumentasjonsbehovRepository) : SøknadService {

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

        val databaseDokumentasjonsbehov = DatabaseDokumentasjonsbehov(søknadId = lagretSkjema.id,
                                                                      data = objectMapper.writeValueAsString(dokumentasjonsbehov))
        dokumentasjonsbehovRepository.save(databaseDokumentasjonsbehov)
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

    @Transactional
    override fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering {
        val søknadDb = SøknadMapper.fromDto(skjemaForArbeidssøker)
        val lagretSkjema = soknadRepository.save(søknadDb)
        logger.info("Mottatt skjema med id ${lagretSkjema.id}")

        return Kvittering(søknadDb.id, "Skjema er mottatt og lagret med id ${lagretSkjema.id}.")
    }

    override fun hentDokumentasjonsbehovForSøknad(søknadId: UUID): DokumentasjonsbehovDto {
        val dokumentasjonsbehov: List<Dokumentasjonsbehov> =
                objectMapper.readValue(dokumentasjonsbehovRepository.findByIdOrNull(søknadId.toString())?.data
                                       ?: throw ApiFeil("Fant ikke dokumentasjonsbehov for søknad $søknadId",
                                                        HttpStatus.BAD_REQUEST))
        val søknad: Soknad =
                soknadRepository.findByIdOrNull(søknadId.toString()) ?: throw ApiFeil("Fant ikke søknad for id $søknadId",
                                                                                      HttpStatus.BAD_REQUEST)

        return DokumentasjonsbehovDto(dokumentasjonsbehov = dokumentasjonsbehov,
                                      innsendingstidspunkt = søknad.opprettetTid,
                                      personIdent = søknad.fnr,
                                      søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype))
    }

    override fun lagreSøknad(soknad: Soknad) {
        soknadRepository.save(soknad)
    }
}

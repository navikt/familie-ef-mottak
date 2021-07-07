package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper.fromDto
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.ef.mottak.repository.domain.EttersendingDb
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class EttersendingService(private val ettersendingRepository: EttersendingRepository,
                          private val vedleggRepository: VedleggRepository,
                          private val dokumentasjonsbehovRepository: DokumentasjonsbehovRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun mottaEttersending(ettersending: EttersendingMedVedlegg<Ettersending>, vedlegg: Map<String, ByteArray>): Kvittering {

        val ettersendingDb = EttersendingMapper.fromDto(ettersending.ettersending, skalBehandlesINySaksbehandling(ettersending))

        val vedlegg = mapVedlegg(ettersendingDb.id, ettersending.vedlegg, vedlegg)
        return motta(ettersendingDb, vedlegg, ettersending.dokumentasjonsbehov)

    }

    private fun <T : Any> skalBehandlesINySaksbehandling(ettersending: EttersendingMedVedlegg<T>): Boolean {
        val erIDev = System.getenv("NAIS_CLUSTER_NAME") == "dev-fss"
        return when {
            erIDev -> ettersending.behandleINySaksbehandling
            else -> false
        }
    }

    private fun mapVedlegg(søknadDbId: String,
                           vedleggMetadata: List<Vedlegg>,
                           vedlegg: Map<String, ByteArray>): List<no.nav.familie.ef.mottak.repository.domain.Vedlegg> =
        vedleggMetadata.map {
            no.nav.familie.ef.mottak.repository.domain.Vedlegg(
                id = UUID.fromString(it.id),
                søknadId = søknadDbId,
                navn = it.navn,
                tittel = it.tittel,
                innhold = Fil(vedlegg[it.id] ?: error("Finner ikke vedlegg med id=${it.id}"))
            )
        }

    private fun motta(ettersendingDb: EttersendingDb,
                      vedlegg: List<no.nav.familie.ef.mottak.repository.domain.Vedlegg>,
                      dokumentasjonsbehov: List<no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov>): Kvittering {
        val lagretSkjema = ettersendingRepository.save(ettersendingDb)
        vedleggRepository.saveAll(vedlegg)

        val databaseDokumentasjonsbehov = Dokumentasjonsbehov(søknadId = lagretSkjema.id,
            data = objectMapper.writeValueAsString(dokumentasjonsbehov))
        dokumentasjonsbehovRepository.save(databaseDokumentasjonsbehov)
        logger.info("Mottatt søknad med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Søknad lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }
}

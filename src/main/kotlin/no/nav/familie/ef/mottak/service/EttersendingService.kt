package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.mapper.SøknadMapper.fromDto
import no.nav.familie.ef.mottak.repository.*
import no.nav.familie.ef.mottak.repository.domain.*
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.*
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.math.log

@Service
class EttersendingService(private val ettersendingRepository: EttersendingRepository,
                          private val ettersendingVedleggRepository: EttersendingVedleggRepository,
                          private val ettersendingDokumentasjonsbehovRepository: EttersendingDokumentasjonsbehovRepository
) {

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

    private fun mapVedlegg(ettersendingDbId: String,
                           vedleggMetadata: List<Vedlegg>,
                           vedlegg: Map<String, ByteArray>): List<no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg> =
        vedleggMetadata.map {
            no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg(
                id = UUID.fromString(it.id.dropLast(4)), //TODO kan ikke ha droplast??
                ettersendingId = ettersendingDbId,
                navn = it.navn,
                tittel = it.tittel,
                innhold = Fil(vedlegg[it.id] ?: error("Finner ikke vedlegg med id=${it.id}"))
            )
        }

    private fun motta(ettersendingDb: EttersendingDb,
                      vedlegg: List<no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg>,
                      ettersendingDokumentasjonsbehov: List<no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov>): Kvittering {
        val lagretSkjema = ettersendingRepository.save(ettersendingDb)
        ettersendingVedleggRepository.saveAll(vedlegg)

        val databaseDokumentasjonsbehov = EttersendingDokumentasjonsbehov(ettersendingId = lagretSkjema.id,
            data = objectMapper.writeValueAsString(ettersendingDokumentasjonsbehov))
        ettersendingDokumentasjonsbehovRepository.save(databaseDokumentasjonsbehov)
        logger.info("Mottatt søknad med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Søknad lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }
}

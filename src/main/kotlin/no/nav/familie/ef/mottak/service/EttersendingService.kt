package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.integration.FamilieDokumentClient
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.kontrakter.ef.ettersending.EttersendingMedVedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendingResponseData
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EttersendingService(
        private val ettersendingRepository: EttersendingRepository,
        private val ettersendingVedleggRepository: EttersendingVedleggRepository,
        private val dokumentClient: FamilieDokumentClient,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun mottaEttersending(ettersending: EttersendingMedVedlegg): Kvittering {
        val ettersendingDb = EttersendingMapper.fromDto(ettersending.ettersending)
        val vedlegg = mapVedlegg(ettersendingDb.id, ettersending.vedlegg)

        return motta(ettersendingDb, vedlegg)
    }

    fun hentEttersendingsdataForPerson(personIdent: PersonIdent): List<EttersendingResponseData> {

        return ettersendingRepository.findAllByFnr(personIdent.ident).map {
            EttersendingResponseData(objectMapper.readValue(it.ettersendingJson), it.opprettetTid)
        }
    }

    private fun mapVedlegg(ettersendingDbId: UUID,
                           vedleggMetadata: List<Vedlegg>): List<EttersendingVedlegg> =
            vedleggMetadata.map {
                EttersendingVedlegg(
                        id = UUID.fromString(it.id),
                        ettersendingId = ettersendingDbId,
                        navn = it.navn,
                        tittel = it.tittel,
                        innhold = Fil(dokumentClient.hentVedlegg(it.id))
                )
            }

    private fun motta(
            ettersendingDb: Ettersending,
            vedlegg: List<EttersendingVedlegg>,
    ): Kvittering {
        val lagretSkjema = ettersendingRepository.save(ettersendingDb)
        ettersendingVedleggRepository.saveAll(vedlegg)
        logger.info("Mottatt ettersending med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id.toString(), "Ettersending lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }

    fun hentEttersending(id: String): Ettersending{
        return ettersendingRepository.findByIdOrNull(UUID.fromString(id)) ?: error("Ugyldig primærnøkkel")
    }

    fun lagreEttersending(ettersending: Ettersending) {
        ettersendingRepository.save(ettersending)
    }
}

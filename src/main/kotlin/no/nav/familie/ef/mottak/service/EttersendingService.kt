package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.integration.FamilieDokumentClient
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.ef.felles.StønadType
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
    fun mottaEttersending(ettersendingMap: Map<StønadType, EttersendelseDto>): Kvittering {
        ettersendingMap.forEach { (stønadType, ettersending) ->
            val ettersendingDb = EttersendingMapper.fromDto(stønadType, ettersending)
            val vedlegg = mapVedlegg(ettersendingDb.id, ettersending)
            motta(ettersendingDb, vedlegg)
        }
        return Kvittering(UUID.randomUUID().toString(), "Ettersending er registrert mottatt.")
    }

    fun hentEttersendingsdataForPerson(personIdent: PersonIdent): List<EttersendelseDto> {

        return ettersendingRepository.findAllByFnr(personIdent.ident).map {
            objectMapper.readValue(it.ettersendingJson.data)
        }
    }

    private fun mapVedlegg(
        ettersendingDbId: UUID,
        ettersending: EttersendelseDto
    ): List<EttersendingVedlegg> =
        ettersending.dokumentasjonsbehov.flatMap { dokumentasjonsbehov ->
            dokumentasjonsbehov.vedlegg.map { vedlegg ->
                EttersendingVedlegg(
                    id = UUID.fromString(vedlegg.id),
                    ettersendingId = ettersendingDbId,
                    navn = vedlegg.navn,
                    tittel = vedlegg.tittel,
                    innhold = EncryptedFile(dokumentClient.hentVedlegg(vedlegg.id))
                )
            }
        }

    private fun motta(
        ettersendingDb: Ettersending,
        vedlegg: List<EttersendingVedlegg>,
    ) {
        val lagretSkjema = ettersendingRepository.insert(ettersendingDb)
        ettersendingVedleggRepository.insertAll(vedlegg)
        logger.info("Mottatt ettersending med id ${lagretSkjema.id}")
    }

    fun hentEttersending(id: String): Ettersending {
        return ettersendingRepository.findByIdOrNull(UUID.fromString(id)) ?: error("Ugyldig primærnøkkel")
    }

    fun oppdaterEttersending(ettersending: Ettersending) {
        ettersendingRepository.update(ettersending)
    }
}

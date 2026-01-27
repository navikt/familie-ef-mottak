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
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import no.nav.familie.kontrakter.ef.ettersending.EttersendelseDto
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.jdbc.core.JdbcAggregateOperations
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class EttersendingService(
    private val ettersendingRepository: EttersendingRepository,
    private val entityOperations: JdbcAggregateOperations,
    private val ettersendingVedleggRepository: EttersendingVedleggRepository,
    private val dokumentClient: FamilieDokumentClient,
    private val taskProsesseringService: TaskProsesseringService,
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

    fun hentEttersendingsdataForPerson(personIdent: PersonIdent): List<EttersendelseDto> =
        ettersendingRepository.findAllByFnr(personIdent.ident).map {
            objectMapper.readValue(it.ettersendingJson.data)
        }

    fun hentEttersendingerForPerson(personIdent: PersonIdent): List<Ettersending> = ettersendingRepository.findAllByFnr(personIdent.ident)

    private fun mapVedlegg(
        ettersendingDbId: UUID,
        ettersending: EttersendelseDto,
    ): List<EttersendingVedlegg> =
        ettersending.dokumentasjonsbehov.flatMap { dokumentasjonsbehov ->
            dokumentasjonsbehov.vedlegg.map { vedlegg ->
                EttersendingVedlegg(
                    id = UUID.fromString(vedlegg.id),
                    ettersendingId = ettersendingDbId,
                    navn = vedlegg.navn,
                    tittel = vedlegg.tittel,
                    innhold = EncryptedFile(dokumentClient.hentVedlegg(vedlegg.id)),
                )
            }
        }

    private fun motta(
        ettersendingDb: Ettersending,
        vedlegg: List<EttersendingVedlegg>,
    ) {
        val lagretEttersending = entityOperations.insert(ettersendingDb)
        ettersendingVedleggRepository.insertAll(vedlegg)
        taskProsesseringService.startTaskProsessering(lagretEttersending)
        logger.info("Mottatt ettersending med id ${lagretEttersending.id}")
    }

    fun hentEttersending(id: String): Ettersending = ettersendingRepository.findByIdOrNull(UUID.fromString(id)) ?: error("Ugyldig primærnøkkel")

    fun oppdaterEttersending(ettersending: Ettersending) {
        entityOperations.update(ettersending)
    }

    fun slettEttersending(ettersendingId: UUID) {
        val ettersending = ettersendingRepository.findByIdOrNull(ettersendingId) ?: return
        if (ettersending.journalpostId == null) {
            throw IllegalStateException("Ettersending $ettersendingId er ikke journalført og kan ikke slettes.")
        }
        ettersendingVedleggRepository.deleteAllByEttersendingId(ettersendingId)
        ettersendingRepository.deleteById(ettersendingId)
    }

    /**
     * Denne trengs i spesialtilfeller når vi får ettersendinger med flere store filer som gjør at
     * journalføring feiler med OutOfMemory (OOM).
     *
     * Denne funksjonen kan splitte ut en stor fil til en egen ettersending og således lettere få igjennom
     * journalføringer på generelt grunnlag.
     *
     * Det vil bli opprettet en egen Task for denne ettersendingen automatisk ca 10-60 sekunder
     * etter at ettersendingen er opprettet.
     */
    @Transactional
    fun trekkUtEttersendingTilEgenTaskForVedlegg(ettersendingVedleggId: UUID): UUID {
        val ettersendingId = ettersendingVedleggRepository.findEttersendingIdById(ettersendingVedleggId)
        val ettersending = ettersendingRepository.findByIdOrThrow(ettersendingId)
        val nyEttersending =
            ettersending.copy(
                id = UUID.randomUUID(),
                taskOpprettet = false,
            )
        entityOperations.insert(nyEttersending)
        val antallOppdatert =
            ettersendingVedleggRepository.oppdaterEttersendingIdForVedlegg(
                id = ettersendingVedleggId,
                ettersendingId = nyEttersending.id,
            )
        if (antallOppdatert != 1) {
            error("Fikk enten oppdatert for mange eller for få rader i ettersendingVedlegg. Antall oppdatert: $antallOppdatert")
        }
        return nyEttersending.id
    }
}

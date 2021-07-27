package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.dto.EttersendingRequestData
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.mapper.EttersendingMapper
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.EttersendingVedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Ettersending
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.kontrakter.ef.ettersending.EttersendingMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.Vedlegg
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import no.nav.familie.kontrakter.felles.PersonIdent

@Service
class EttersendingService(private val ettersendingRepository: EttersendingRepository,
                          private val ettersendingVedleggRepository: EttersendingVedleggRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun mottaEttersending(ettersending: EttersendingMedVedlegg, vedlegg: Map<String, ByteArray>): Kvittering {

        val ettersendingDb = EttersendingMapper.fromDto(ettersending.ettersending)
        val vedlegg = mapVedlegg(ettersendingDb.id, ettersending.vedlegg, vedlegg)

        return motta(ettersendingDb, vedlegg)
    }


    fun hentEttersendingsdataForPerson(personIdent: PersonIdent): List<EttersendingRequestData> {

        return ettersendingRepository.findAllByFnr(personIdent.ident).map {
            EttersendingRequestData(objectMapper.readValue(it.ettersendingJson), it.opprettetTid)
        }
    }

    private fun mapVedlegg(ettersendingDbId: String,
                           vedleggMetadata: List<Vedlegg>,
                           vedlegg: Map<String, ByteArray>): List<no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg> =
            vedleggMetadata.map {
                no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg(
                        id = UUID.fromString((it.id)),
                        //id = UUID.fromString(it.id.dropLast(4)), //Må bruke denne for at det skal funke med postman
                        ettersendingId = ettersendingDbId,
                        navn = it.navn,
                        tittel = it.tittel,
                        innhold = Fil(vedlegg[it.id] ?: error("Finner ikke vedlegg med id=${it.id}"))
                )
            }

    private fun motta(
            ettersendingDb: Ettersending,
            vedlegg: List<no.nav.familie.ef.mottak.repository.domain.EttersendingVedlegg>,
    ): Kvittering {
        val lagretSkjema = ettersendingRepository.save(ettersendingDb)
        ettersendingVedleggRepository.saveAll(vedlegg)
        logger.info("Mottatt ettersending med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Ettersending lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }

}

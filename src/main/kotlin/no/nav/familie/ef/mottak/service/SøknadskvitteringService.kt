package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.FamilieDokumentClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.kontrakter.ef.ettersending.SøknadMedDokumentasjonsbehovDto
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.kontrakter.felles.PersonIdent
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.søknad.SistInnsendtSøknadDto
import no.nav.familie.kontrakter.felles.søknad.nyereEnn
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov as DatabaseDokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.Vedlegg as VedleggFamilieKontrakter

@Service
@Transactional
class SøknadskvitteringService(
    private val søknadRepository: SøknadRepository,
    private val vedleggRepository: VedleggRepository,
    private val dokumentClient: FamilieDokumentClient,
    private val dokumentasjonsbehovRepository: DokumentasjonsbehovRepository,
    private val taskProsesseringService: TaskProsesseringService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun mottaOvergangsstønad(søknad: SøknadMedVedlegg<SøknadOvergangsstønad>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad, true)
        val vedlegg = mapSøknadsvedlegg(søknadDb.id, søknad.vedlegg)
        return mottaSøknad(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    fun mottaBarnetilsyn(søknad: SøknadMedVedlegg<SøknadBarnetilsyn>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad, true)
        val vedlegg = mapSøknadsvedlegg(søknadDb.id, søknad.vedlegg)
        return mottaSøknad(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    fun mottaSkolepenger(søknad: SøknadMedVedlegg<SøknadSkolepenger>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad, true)
        val vedlegg = mapSøknadsvedlegg(søknadDb.id, søknad.vedlegg)
        return mottaSøknad(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    fun mottaArbeidssøkerSkjema(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering {
        val søknadDb = SøknadMapper.fromDto(skjemaForArbeidssøker)
        val lagretSkjema = søknadRepository.insert(søknadDb)
        taskProsesseringService.startPdfKvitteringTaskProsessering(lagretSkjema)
        logger.info("Mottatt skjema med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Pdf-skjema lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }

    fun hentSøknad(søknadId: String): Søknad = søknadRepository.findByIdOrThrow(søknadId)

    fun hentBarnetilsynSøknadsverdierTilGjenbruk(personIdent: String): SøknadBarnetilsyn? {
        val søknadFraDb = søknadRepository.finnSisteSøknadForPersonOgStønadstype(personIdent, DOKUMENTTYPE_BARNETILSYN)
        return if (søknadFraDb?.søknadJson?.data != null) {
            objectMapper.readValue<SøknadBarnetilsyn>(søknadFraDb.søknadJson.data)
        } else {
            null
        }
    }

    fun hentSøknaderForPerson(personIdent: PersonIdent): List<Søknad> = søknadRepository.findAllByFnr(personIdent.ident)

    fun hentDokumentasjonsbehovForPerson(personIdent: String): List<SøknadMedDokumentasjonsbehovDto> =
        søknadRepository
            .finnSisteSøknadenPerStønadtype(personIdent)
            .filter { SøknadType.hentSøknadTypeForDokumenttype(it.dokumenttype).harDokumentasjonsbehov }
            .map {
                SøknadMedDokumentasjonsbehovDto(
                    søknadId = it.id,
                    stønadType =
                        StønadType
                            .valueOf(
                                SøknadType
                                    .hentSøknadTypeForDokumenttype(it.dokumenttype)
                                    .toString(),
                            ),
                    søknadDato = it.opprettetTid.toLocalDate(),
                    dokumentasjonsbehov = hentDokumentasjonsbehovForSøknad(it),
                )
            }

    fun hentDokumentasjonsbehovForSøknad(søknad: Søknad): DokumentasjonsbehovDto {
        val dokumentasjonsbehovJson = dokumentasjonsbehovRepository.findByIdOrNull(søknad.id)
        val dokumentasjonsbehov: List<Dokumentasjonsbehov> =
            dokumentasjonsbehovJson?.let {
                objectMapper.readValue(it.data)
            } ?: emptyList()

        return DokumentasjonsbehovDto(
            dokumentasjonsbehov = dokumentasjonsbehov,
            innsendingstidspunkt = søknad.opprettetTid,
            personIdent = søknad.fnr,
            søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype),
        )
    }

    fun oppdaterSøknad(søknad: Søknad) {
        søknadRepository.update(søknad)
    }

    @Transactional
    fun reduserSøknad(søknadId: String) {
        val søknad = søknadRepository.findByIdOrNull(søknadId) ?: return
        if (søknad.journalpostId == null) {
            throw IllegalStateException("Søknad $søknadId er ikke journalført og kan ikke reduseres.")
        }
        vedleggRepository.deleteBySøknadId(søknadId)
        dokumentasjonsbehovRepository.deleteById(søknadId)
        val t = søknad.copy(søknadPdf = null)
        søknadRepository.update(t)
    }

    @Transactional
    fun slettSøknad(søknadId: String) {
        val søknad = søknadRepository.findByIdOrNull(søknadId) ?: return
        if (søknad.journalpostId == null) {
            throw IllegalStateException("Søknad $søknadId er ikke journalført og kan ikke slettes.")
        }
        søknadRepository.deleteById(søknadId)
    }

    @Transactional
    fun hentSistInnsendtSøknadPerStønad(personIdent: String): List<SistInnsendtSøknadDto> {
        val stønadstyper =
            listOf(
                DOKUMENTTYPE_BARNETILSYN,
                DOKUMENTTYPE_OVERGANGSSTØNAD,
                DOKUMENTTYPE_SKOLEPENGER,
            )

        return stønadstyper
            .mapNotNull { dokumenttype ->
                val søknad =
                    søknadRepository.finnSisteSøknadForPersonOgStønadstype(
                        fnr = personIdent,
                        stønadstype = dokumenttype,
                    )
                val stønadType = dokumenttypeTilStønadType(dokumenttype) ?: error("Kunne ikke mappe dokumenttype=$dokumenttype til stønadstype")

                if (søknad != null) {
                    SistInnsendtSøknadDto(
                        søknadsdato = søknad.opprettetTid.toLocalDate(),
                        stønadType = stønadType,
                    )
                } else {
                    null
                }
            }.filter { it.nyereEnn() }
    }

    private fun mapSøknadsvedlegg(
        søknadDbId: String,
        vedleggMetadata: List<VedleggFamilieKontrakter>,
    ): List<Vedlegg> =
        vedleggMetadata.map {
            Vedlegg(
                id = UUID.fromString(it.id),
                søknadId = søknadDbId,
                navn = it.navn,
                tittel = it.tittel,
                innhold = EncryptedFile(dokumentClient.hentVedlegg(it.id)),
            )
        }

    private fun mottaSøknad(
        søknadDb: Søknad,
        vedlegg: List<Vedlegg>,
        dokumentasjonsbehov: List<Dokumentasjonsbehov>,
    ): Kvittering {
        val lagretSkjema = søknadRepository.insert(søknadDb)
        vedleggRepository.insertAll(vedlegg)
        taskProsesseringService.startPdfKvitteringTaskProsessering(lagretSkjema)
        val databaseDokumentasjonsbehov =
            DatabaseDokumentasjonsbehov(
                søknadId = lagretSkjema.id,
                data = objectMapper.writeValueAsString(dokumentasjonsbehov),
            )
        dokumentasjonsbehovRepository.insert(databaseDokumentasjonsbehov)
        logger.info("Mottatt pdf-søknad med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Pdf-søknad lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }
}

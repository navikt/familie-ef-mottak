package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.api.ApiFeil
import no.nav.familie.ef.mottak.api.dto.Kvittering
import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.integration.FamilieDokumentClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.DokumentasjonsbehovRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Fil
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import no.nav.familie.kontrakter.ef.ettersending.SøknadMedDokumentasjonsbehovDto
import no.nav.familie.kontrakter.ef.felles.StønadType
import no.nav.familie.kontrakter.ef.søknad.Dokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadMedVedlegg
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import no.nav.familie.kontrakter.ef.søknad.SøknadType
import no.nav.familie.kontrakter.ef.søknad.dokumentasjonsbehov.DokumentasjonsbehovDto
import no.nav.familie.kontrakter.felles.objectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID
import no.nav.familie.ef.mottak.repository.domain.Dokumentasjonsbehov as DatabaseDokumentasjonsbehov
import no.nav.familie.kontrakter.ef.søknad.Vedlegg as VedleggKontrakt

@Service
class SøknadService(private val søknadRepository: SøknadRepository,
                    private val vedleggRepository: VedleggRepository,
                    private val dokumentClient: FamilieDokumentClient,
                    private val dokumentasjonsbehovRepository: DokumentasjonsbehovRepository,
                    private val featureToggleService: FeatureToggleService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun mottaOvergangsstønad(søknad: SøknadMedVedlegg<SøknadOvergangsstønad>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad,
                                            skalBehandlesINySaksbehandling(søknad) || erAktuellForFørsteSak(søknad.søknad))
        val vedlegg = mapVedlegg(søknadDb.id, søknad.vedlegg)
        return motta(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    fun mottaBarnetilsyn(søknad: SøknadMedVedlegg<SøknadBarnetilsyn>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad, skalBehandlesINySaksbehandling(søknad))
        val vedlegg = mapVedlegg(søknadDb.id, søknad.vedlegg)
        return motta(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    @Transactional
    fun mottaSkolepenger(søknad: SøknadMedVedlegg<SøknadSkolepenger>): Kvittering {
        val søknadDb = SøknadMapper.fromDto(søknad.søknad, skalBehandlesINySaksbehandling(søknad))
        val vedlegg = mapVedlegg(søknadDb.id, søknad.vedlegg)
        return motta(søknadDb, vedlegg, søknad.dokumentasjonsbehov)
    }

    private fun <T : Any> skalBehandlesINySaksbehandling(søknad: SøknadMedVedlegg<T>): Boolean {
        val erIDev = System.getenv("NAIS_CLUSTER_NAME") == "dev-fss"
        return when {
            erIDev -> søknad.behandleINySaksbehandling
            else -> false
        }
    }

    private fun erAktuellForFørsteSak(søknad: SøknadOvergangsstønad): Boolean {
        val erAktuellForFørsteSakFT = featureToggleService.isEnabled("familie.ef.mottak.er-aktuell-for-forste-sak")
        logger.info("Skal sjekke om søknad er aktuell for å plukkes som første sak i ny løsning: $erAktuellForFørsteSakFT")

        if (!erAktuellForFørsteSakFT) {
            return false
        }
        val dagensDato = LocalDate.now()


        return søknad.barn.verdi.any {
            val fødselsTermindato = it.fødselTermindato?.verdi ?: LocalDate.MIN
            val fødselsdatoFraIdent = it.fødselsnummer?.verdi?.fødselsdato ?: LocalDate.MIN
            val alderBasertPåTermindatoErMindreEnn6mnd = dagensDato.minusMonths(6).isBefore(fødselsTermindato)
            val alderBasertPåIdentErMindreEnn6mnd = dagensDato.minusMonths(6).isBefore(fødselsdatoFraIdent)
            alderBasertPåTermindatoErMindreEnn6mnd || alderBasertPåIdentErMindreEnn6mnd
        }
    }

    private fun motta(søknadDb: Søknad,
                      vedlegg: List<Vedlegg>,
                      dokumentasjonsbehov: List<Dokumentasjonsbehov>): Kvittering {
        val lagretSkjema = søknadRepository.save(søknadDb)
        vedleggRepository.saveAll(vedlegg)

        val databaseDokumentasjonsbehov = DatabaseDokumentasjonsbehov(søknadId = lagretSkjema.id,
                                                                      data = objectMapper.writeValueAsString(dokumentasjonsbehov))
        dokumentasjonsbehovRepository.save(databaseDokumentasjonsbehov)
        logger.info("Mottatt søknad med id ${lagretSkjema.id}")
        return Kvittering(lagretSkjema.id, "Søknad lagret med id ${lagretSkjema.id} er registrert mottatt.")
    }

    private fun mapVedlegg(søknadDbId: String,
                           vedleggMetadata: List<VedleggKontrakt>): List<Vedlegg> =
            vedleggMetadata.map {
                Vedlegg(id = UUID.fromString(it.id),
                        søknadId = søknadDbId,
                        navn = it.navn,
                        tittel = it.tittel,
                        innhold = Fil(dokumentClient.hentVedlegg(it.id)))
            }

    fun get(id: String): Søknad {
        return søknadRepository.findByIdOrNull(id) ?: error("Ugyldig primærnøkkel")
    }

    @Transactional
    fun motta(skjemaForArbeidssøker: SkjemaForArbeidssøker): Kvittering {
        val søknadDb = SøknadMapper.fromDto(skjemaForArbeidssøker)
        val lagretSkjema = søknadRepository.save(søknadDb)
        logger.info("Mottatt skjema med id ${lagretSkjema.id}")

        return Kvittering(søknadDb.id, "Skjema er mottatt og lagret med id ${lagretSkjema.id}.")
    }

    fun hentDokumentasjonsbehovForPerson(personIdent: String): List<SøknadMedDokumentasjonsbehovDto> {
        return søknadRepository.findAllByFnr(personIdent)
                .filter { SøknadType.hentSøknadTypeForDokumenttype(it.dokumenttype).harDokumentasjonsbehov }
                .map {
                    SøknadMedDokumentasjonsbehovDto(søknadId = it.id,
                                                    stønadType = StønadType.valueOf(SøknadType.hentSøknadTypeForDokumenttype(it.dokumenttype)
                                                                                            .toString()),
                                                    søknadDato = it.opprettetTid.toLocalDate(),
                                                    dokumentasjonsbehov = hentDokumentasjonsbehovForSøknad(
                                                            UUID.fromString(it.id)))
                }
    }

    // Gamle søknader har ikke dokumentasjonsbehov - de må returnere tom liste
    fun hentDokumentasjonsbehovForSøknad(søknadId: UUID): DokumentasjonsbehovDto {
        val dokumentasjonsbehovJson = dokumentasjonsbehovRepository.findByIdOrNull(søknadId.toString())
        val dokumentasjonsbehov: List<Dokumentasjonsbehov> = dokumentasjonsbehovJson?.let {
            objectMapper.readValue(it.data)
        } ?: emptyList()
        val søknad: Søknad =
                søknadRepository.findByIdOrNull(søknadId.toString()) ?: throw ApiFeil("Fant ikke søknad for id $søknadId",
                                                                                      HttpStatus.BAD_REQUEST)

        return DokumentasjonsbehovDto(dokumentasjonsbehov = dokumentasjonsbehov,
                                      innsendingstidspunkt = søknad.opprettetTid,
                                      personIdent = søknad.fnr,
                                      søknadType = SøknadType.hentSøknadTypeForDokumenttype(søknad.dokumenttype))
    }

    fun lagreSøknad(søknad: Søknad) {
        søknadRepository.save(søknad)
    }
}

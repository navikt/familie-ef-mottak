package no.nav.familie.ef.mottak.service

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.ef.mottak.config.getValue
import no.nav.familie.ef.mottak.integration.IntegrasjonerClient
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.service.MappeSøkestreng.SELVSTENDIG
import no.nav.familie.ef.mottak.service.MappeSøkestreng.SÆRLIG_TILSYNSKREVENDE
import no.nav.familie.ef.mottak.service.MappeSøkestreng.UPLASSERT
import no.nav.familie.ef.mottak.util.dokumenttypeTilStønadType
import no.nav.familie.kontrakter.ef.søknad.Aktivitet
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.Søknadsfelt
import no.nav.familie.kontrakter.felles.ef.StønadType
import no.nav.familie.kontrakter.felles.objectMapper
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

@Service
class MappeService(
    private val integrasjonerClient: IntegrasjonerClient,
    private val søknadskvitteringService: SøknadskvitteringService,
    private val cacheManager: CacheManager,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun finnMappeIdForSøknadOgEnhet(
        søknadId: String,
        enhetsnummer: String?,
    ): Long? {
        if (enhetsnummer != OppgaveService.ENHETSNUMMER_NAY) {
            return null
        }
        val mapper = finnMapperForEnhet(enhetsnummer)
        val søkestreng = finnSøkestrengForSøknad(søknadId)
        return utledMappeForSøkestreng(mapper, søkestreng)?.id?.toLong()
    }

    private fun finnMapperForEnhet(enhetsnummer: String): FinnMappeResponseDto =
        cacheManager.getValue("oppgave-mappe", enhetsnummer) {
            val finnMappeRequest =
                FinnMappeRequest(
                    tema = listOf(),
                    enhetsnr = enhetsnummer,
                    opprettetFom = null,
                    limit = 1000,
                )
            val mapperResponse = integrasjonerClient.finnMappe(finnMappeRequest)
            log.info("Mapper funnet: Antall: ${mapperResponse.antallTreffTotalt}, ${mapperResponse.mapper} ")

            mapperResponse
        }

    private fun finnSøkestrengForSøknad(søknadId: String): MappeSøkestreng {
        val søknad = søknadskvitteringService.hentSøknad(søknadId)

        return when (dokumenttypeTilStønadType(søknad.dokumenttype)) {
            StønadType.OVERGANGSSTØNAD -> mappeFraOvergangsstønad(søknad)
            StønadType.BARNETILSYN -> mappeFraBarnetilsyn(søknad)
            StønadType.SKOLEPENGER -> UPLASSERT
            else -> UPLASSERT
        }
    }

    private fun utledMappeForSøkestreng(
        mapperResponse: FinnMappeResponseDto,
        søkestreng: MappeSøkestreng,
    ): MappeDto? =
        if (UPLASSERT == søkestreng) {
            null
        } else {
            mapperResponse.mapper
                .filter {
                    !it.navn.contains("EF Sak", true) &&
                        it.navn.contains(søkestreng.verdi, true)
                }.maxByOrNull { it.id }
                ?: error("Fant ikke mappe for $søkestreng")
        }

    private fun mappeFraOvergangsstønad(søknad: Søknad): MappeSøkestreng {
        val søknadsdata = objectMapper.readValue<SøknadOvergangsstønad>(søknad.søknadJson.data)
        return when {
            erSærligTilsynskrevende(søknadsdata) -> SÆRLIG_TILSYNSKREVENDE
            erSelvstendig(søknadsdata.aktivitet) -> SELVSTENDIG
            else -> UPLASSERT
        }
    }

    private fun erSærligTilsynskrevende(søknadsdata: SøknadOvergangsstønad) =
        søknadsdata.situasjon.verdi.barnMedSærligeBehov
            ?.verdi != null

    private fun mappeFraBarnetilsyn(søknad: Søknad): MappeSøkestreng {
        val søknadsdata = objectMapper.readValue<SøknadBarnetilsyn>(søknad.søknadJson.data)
        return when {
            erSærligTilsynskrevende(søknadsdata) -> SÆRLIG_TILSYNSKREVENDE
            erSelvstendig(søknadsdata.aktivitet) -> SELVSTENDIG
            else -> UPLASSERT
        }
    }

    private fun erSærligTilsynskrevende(søknadsdata: SøknadBarnetilsyn) =
        søknadsdata.barn.verdi.any {
            it.barnepass
                ?.verdi
                ?.årsakBarnepass
                ?.svarId == "trengerMerPassEnnJevnaldrede"
        }

    private fun erSelvstendig(aktivitet: Søknadsfelt<Aktivitet>) =
        aktivitet.verdi.firmaer
            ?.verdi
            ?.isNotEmpty() ?: false ||
            aktivitet.verdi.virksomhet?.verdi != null
}

enum class MappeSøkestreng(
    val verdi: String,
) {
    SÆRLIG_TILSYNSKREVENDE("60 Særlig tilsynskrevende"),
    SELVSTENDIG("61 Selvstendig næringsdrivende"),
    UPLASSERT("Uplassert"),
}

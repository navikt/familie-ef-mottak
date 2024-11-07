package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.ef.mottak.repository.util.findByIdOrThrow
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDateTime

@Service
@Transactional
class SøknadskvitteringService(
    private val søknadRepository: SøknadRepository,
    private val vedleggRepository: VedleggRepository,
) {
    fun hentSøknadOgMapTilGenereltFormat(søknadId: String): Map<String, Any> {
        val søknad = hentSøknad(søknadId)
        val vedleggstitler = hentVedleggstitlerForSøknad(søknadId)
        return mapSøknadTilGenereltFormat(søknad, vedleggstitler)
    }

    private fun hentSøknad(søknadId: String): Søknad = søknadRepository.findByIdOrThrow(søknadId)

    private fun hentVedleggstitlerForSøknad(søknadId: String): List<String> = vedleggRepository.finnTitlerForSøknadId(søknadId).sorted()

    private fun mapSøknadTilGenereltFormat(
        innsending: Søknad,
        vedleggTitler: List<String>,
    ): Map<String, Any> =
        when (innsending.dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> {
                val dto = SøknadMapper.toDto<SøknadOvergangsstønad>(innsending)
                SøknadTreeWalker.mapOvergangsstønad(dto, vedleggTitler)
            }
            DOKUMENTTYPE_BARNETILSYN -> {
                val dto = SøknadMapper.toDto<SøknadBarnetilsyn>(innsending)
                SøknadTreeWalker.mapBarnetilsyn(dto, vedleggTitler)
            }
            DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER -> {
                val dto = SøknadMapper.toDto<SkjemaForArbeidssøker>(innsending)
                SøknadTreeWalker.mapSkjemafelter(dto)
            }
            DOKUMENTTYPE_SKOLEPENGER -> {
                val dto = SøknadMapper.toDto<SøknadSkolepenger>(innsending)
                SøknadTreeWalker.mapSkolepenger(dto, vedleggTitler)
            }
            else -> {
                error("Ukjent eller manglende dokumenttype id: ${innsending.id}")
            }
        }

    fun skrivSistePdfTilFil(): String {
        val søknad = søknadRepository.finnSisteLagredeSøknad()
        val søknadPdf = søknad.søknadPdf ?: error("Søknad som skal skrives til fil har ingen søknadspdf på søknad med id=${søknad.id}")
        søknadPdf.bytes.let {
            File("soknadsKvitteringTest/søknad${LocalDateTime.now()}.pdf").writeBytes(it)
        }
        return "Ok"
    }
}

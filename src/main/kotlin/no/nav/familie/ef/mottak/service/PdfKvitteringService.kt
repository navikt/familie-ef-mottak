package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_BARNETILSYN
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_OVERGANGSSTØNAD
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKJEMA_ARBEIDSSØKER
import no.nav.familie.ef.mottak.config.DOKUMENTTYPE_SKOLEPENGER
import no.nav.familie.ef.mottak.integration.PdfKvitteringClient
import no.nav.familie.ef.mottak.mapper.SøknadMapper
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Søknad
import no.nav.familie.kontrakter.ef.søknad.SkjemaForArbeidssøker
import no.nav.familie.kontrakter.ef.søknad.SøknadBarnetilsyn
import no.nav.familie.kontrakter.ef.søknad.SøknadOvergangsstønad
import no.nav.familie.kontrakter.ef.søknad.SøknadSkolepenger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PdfKvitteringService(
    private val søknadRepository: SøknadRepository,
    private val vedleggRepository: VedleggRepository,
    private val pdfKvitteringClient: PdfKvitteringClient,
) {
    fun lagPdfKvittering(id: String) {
        val innsending = søknadRepository.findByIdOrNull(id) ?: error("Kunne ikke finne søknad ($id) i database")
        val vedleggTitler = vedleggRepository.finnTitlerForSøknadId(id).sorted()
        val feltMap = lagFeltMap(innsending, vedleggTitler)
        val søknadPdf = pdfKvitteringClient.opprettPdf(feltMap)
        val oppdatertSoknad = innsending.copy(søknadPdf = EncryptedFile(søknadPdf))
        søknadRepository.update(oppdatertSoknad)
    }

    private fun lagFeltMap(
        innsending: Søknad,
        vedleggTitler: List<String>,
    ): Map<String, Any> =
        when (innsending.dokumenttype) {
            DOKUMENTTYPE_OVERGANGSSTØNAD -> {
                val dto = SøknadMapper.toDto<SøknadOvergangsstønad>(innsending)
                SøknadTilGenereltFormatMapper.mapOvergangsstønad(dto, vedleggTitler)
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
}

package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.hendelse.JournalfoeringHendelseDbUtil
import no.nav.familie.ef.mottak.hendelse.JournalpostState
import no.nav.familie.ef.mottak.hendelse.getJournalpostState
import no.nav.familie.ef.mottak.hendelse.ikkeGyldigKanalLogString
import no.nav.familie.ef.mottak.hendelse.ikkeGyldigLogString
import no.nav.familie.ef.mottak.hendelse.incrementMetric
import no.nav.familie.ef.mottak.hendelse.skalBehandles
import no.nav.familie.ef.mottak.hendelse.statusIkkeMottattLogString
import no.nav.familie.ef.mottak.repository.EttersendingRepository
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JournalføringsoppgaveService(
    val featureToggleService: FeatureToggleService,
    val søknadRepository: SøknadRepository,
    val ettersendingRepository: EttersendingRepository,
    val journalfoeringHendelseDbUtil: JournalfoeringHendelseDbUtil
) {

    val logger: Logger = LoggerFactory.getLogger(JournalføringsoppgaveService::class.java)

    fun lagEksternJournalføringTask(journalpost: Journalpost) {
        if (journalpost.skalBehandles()) {
            opprettTaskDersomDetIkkeAlleredeFinnes(journalpost)
        } else {
            logJournalpost(journalpost)
        }

        journalpost.incrementMetric()
    }

    private fun opprettTaskDersomDetIkkeAlleredeFinnes(journalpost: Journalpost) {
        val søknad = søknadRepository.findByJournalpostId(journalpost.journalpostId)
        val ettersending = ettersendingRepository.findByJournalpostId(journalpost.journalpostId)
        if (søknad == null && ettersending == null) {
            journalfoeringHendelseDbUtil.lagreEksternJournalføringsTask(journalpost)
        } else if (ettersending != null) {
            logger.info("Hendelse mottatt for digital ettersending - journalpostId=${journalpost.journalpostId}")
        } else if (søknad != null) {
            logger.info("Hendelse mottatt for digital søknad ${søknad.id}")
        }
    }

    private fun logJournalpost(journalpost: Journalpost) {
        when (journalpost.getJournalpostState()) {
            JournalpostState.IKKE_MOTTATT -> logger.info(journalpost.statusIkkeMottattLogString())
            JournalpostState.UGYLDIG_KANAL -> logger.error(journalpost.ikkeGyldigKanalLogString())
            JournalpostState.UGYLDIG -> logger.error(journalpost.ikkeGyldigLogString())
            JournalpostState.GYLDIG -> {} // Ingen logging
        }
    }
}

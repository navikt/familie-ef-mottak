package no.nav.familie.ef.mottak.service

import no.nav.familie.ef.mottak.featuretoggle.FeatureToggleService
import no.nav.familie.ef.mottak.hendelse.*
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JournalføringsoppgaveService(val featureToggleService: FeatureToggleService,
                                   val søknadRepository: SøknadRepository,
                                   val journalfoeringHendelseDbUtil: JournalfoeringHendelseDbUtil) {

    val logger: Logger = LoggerFactory.getLogger(JournalføringsoppgaveService::class.java)

    fun lagEksternJournalføringTask(journalpost: Journalpost) {

        if (journalpost.skalBehandles()) {
            lagreSomEksternJournalføringsTaskDersomSøknadIkkeFinnes(journalpost)
        } else {
            logJournalpost(journalpost)
        }

        journalpost.incrementMetric()
    }

    private fun lagreSomEksternJournalføringsTaskDersomSøknadIkkeFinnes(journalpost: Journalpost) {
        when (val søknad = søknadRepository.findByJournalpostId(journalpost.journalpostId)) {
            null -> journalfoeringHendelseDbUtil.lagreEksternJournalføringsTask(journalpost)
            else -> logger.info("Hendelse mottatt for digital søknad ${søknad.id}")
        }
    }

    private fun logJournalpost(journalpost: Journalpost) {
        when (journalpost.getJournalpostState()) {
            JournalpostState.IKKE_MOTTATT -> logger.info(journalpost.statusIkkeMottattLogString())
            JournalpostState.UGYLDIG_KANAL -> logger.error(journalpost.ikkeGyldigKanalLogString())
        }
    }
}
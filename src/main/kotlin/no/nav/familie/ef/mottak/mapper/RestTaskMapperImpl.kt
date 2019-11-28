package no.nav.familie.ef.mottak.mapper

import no.nav.familie.ef.mottak.service.SøknadService
import no.nav.familie.prosessering.domene.Task
import no.nav.familie.prosessering.rest.RestTask
import no.nav.familie.prosessering.rest.RestTaskMapper
import org.springframework.stereotype.Component

@Component
class RestTaskMapperImpl(private val søknadService: SøknadService) : RestTaskMapper {

    override fun toDto(task: Task): RestTask {

        val søknad = søknadService.get(task.payloadId.toLong())

        return RestTask(task, søknad.journalpostId, søknad.saksnummer, søknad.fnr)
    }
}
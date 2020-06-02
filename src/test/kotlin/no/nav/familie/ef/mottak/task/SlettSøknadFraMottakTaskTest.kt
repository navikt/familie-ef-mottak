package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.familie.ef.mottak.repository.SoknadRepository
import no.nav.familie.ef.mottak.task.SlettSøknadFraMottakTask
import no.nav.familie.prosessering.domene.Task
import org.junit.jupiter.api.Test
import java.util.*

internal class SlettSøknadFraMottakTaskTest {

    private val soknadRepository: SoknadRepository = mockk(relaxed = true)
    private val slettSøknadFraMottakTask = SlettSøknadFraMottakTask(soknadRepository)

    @Test
    fun `Skal slette søknad fra repository`() {
        val idSomSkalSlettes = "321654987"

        slettSøknadFraMottakTask.doTask(Task.nyTask(type = "", payload = idSomSkalSlettes, properties = Properties()))

        verify { soknadRepository.deleteById(idSomSkalSlettes) }
    }
}

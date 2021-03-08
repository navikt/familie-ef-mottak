package no.nav.familie.ef.mottak.mapper

import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class OpprettOppgaveMapperTest {

    @Test
    internal fun `skal sette frist til 1 dag for journalføringsoppgave opprettet før kl 12`() {
        val gjeldendeTid = LocalDateTime.of(2020, 1, 1, 0, 0)
        val frist = OpprettOppgaveMapper(mockk()).lagFristForOppgave(gjeldendeTid)
        Assertions.assertThat(frist).isEqualTo(gjeldendeTid.plusDays(1).toLocalDate())
    }

    @Test
    internal fun `skal sette frist til 2 dager for journalføringsoppgave opprettet etter kl 12`() {
        val gjeldendeTid = LocalDateTime.of(2020, 1, 1, 13, 0)
        val frist = OpprettOppgaveMapper(mockk()).lagFristForOppgave(gjeldendeTid)
        Assertions.assertThat(frist).isEqualTo(gjeldendeTid.plusDays(2).toLocalDate())
    }

    @Test
    internal fun `skal sette frist til 2 dager for journalføringsoppgave opprettet kl 12`() {
        val gjeldendeTid = LocalDateTime.of(2020, 1, 1, 12, 0)
        val frist = OpprettOppgaveMapper(mockk()).lagFristForOppgave(gjeldendeTid)
        Assertions.assertThat(frist).isEqualTo(gjeldendeTid.plusDays(2).toLocalDate())
    }

}
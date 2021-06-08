package no.nav.familie.ef.mottak.mapper

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class OpprettOppgaveMapperTest {

    private val mapper = OpprettOppgaveMapper(mockk())

    @Test
    internal fun `skal sette frist til 1 dag for journalføringsoppgave opprettet før kl 12`() {
        val gjeldendeTid = LocalDateTime.of(2020, 1, 1, 0, 0)
        val frist = mapper.lagFristForOppgave(gjeldendeTid)
        assertThat(frist).isEqualTo(gjeldendeTid.plusDays(1).toLocalDate())
    }

    @Test
    internal fun `skal sette frist til 2 dager for journalføringsoppgave opprettet etter kl 12`() {
        val gjeldendeTid = LocalDateTime.of(2020, 1, 1, 13, 0)
        val frist = mapper.lagFristForOppgave(gjeldendeTid)
        assertThat(frist).isEqualTo(gjeldendeTid.plusDays(2).toLocalDate())
    }

    @Test
    internal fun `skal sette frist til 2 dager for journalføringsoppgave opprettet kl 12`() {
        val gjeldendeTid = LocalDateTime.of(2020, 1, 1, 12, 0)
        val frist = mapper.lagFristForOppgave(gjeldendeTid)
        assertThat(frist).isEqualTo(gjeldendeTid.plusDays(2).toLocalDate())
    }

    @Test
    fun `Skal sette frist for oppgave`() {
        val frister = listOf<Pair<LocalDateTime, LocalDate>>(
                Pair(torsdag.morgen(), fredagFrist),
                Pair(torsdag.kveld(), mandagFrist),
                Pair(fredag.morgen(), mandagFrist),
                Pair(fredag.kveld(), tirsdagFrist),
                Pair(lørdag.morgen(), tirsdagFrist),
                Pair(lørdag.kveld(), tirsdagFrist),
                Pair(søndag.morgen(), tirsdagFrist),
                Pair(søndag.kveld(), tirsdagFrist),
                Pair(mandag.morgen(), tirsdagFrist),
                Pair(mandag.kveld(), onsdagFrist),
        )

        frister.forEach {
            assertThat(mapper.lagFristForOppgave(it.first)).isEqualTo(it.second)
        }
    }

    private fun LocalDateTime.kveld(): LocalDateTime {
        return this.withHour(20)
    }

    private fun LocalDateTime.morgen(): LocalDateTime {
        return this.withHour(8)
    }

    private val torsdag = LocalDateTime.of(2021, 4, 1, 12, 0)
    private val fredag = LocalDateTime.of(2021, 4, 2, 12, 0)
    private val lørdag = LocalDateTime.of(2021, 4, 3, 12, 0)
    private val søndag = LocalDateTime.of(2021, 4, 4, 12, 0)
    private val mandag = LocalDateTime.of(2021, 4, 5, 12, 0)

    private val fredagFrist = LocalDate.of(2021, 4, 2)
    private val mandagFrist = LocalDate.of(2021, 4, 5)
    private val tirsdagFrist = LocalDate.of(2021, 4, 6)
    private val onsdagFrist = LocalDate.of(2021, 4, 7)

}
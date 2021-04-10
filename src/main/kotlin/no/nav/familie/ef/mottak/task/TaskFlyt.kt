package no.nav.familie.ef.mottak.task

fun kafkaHendelseFlyt() = listOf(LagEksternJournalføringsoppgaveTask.TYPE, SjekkOmJournalpostHarFåttEnSak.TYPE)

fun hovedflyt() = listOf(
    LagPdfTask.TYPE,
    ArkiverSøknadTask.TYPE,
    LagBehandleSakOppgaveTask.TYPE,
    OpprettSakTask.TYPE,
    OppdaterBehandleSakOppgaveTask.TYPE,
    OppdaterJournalføringTask.TYPE,
    FerdigstillJournalføringTask.TYPE
)

fun fallbackFlyt() = listOf(LagJournalføringsoppgaveTask.TYPE, HentSaksnummerFraJoarkTask.TYPE)
fun fallbackTask() = fallbackFlyt().first() // Akkurat nå er alle fallback-tasks det samme, men utvid logikk her dersom det skulle endres

fun String.nesteHovedflytTask() = hovedflyt().zipWithNext().first {this == it.first}.second
fun String.nesteFallbackTask() = fallbackFlyt().zipWithNext().first {this == it.first}.second
fun String.nesteKafkaHendelseFlyt() = kafkaHendelseFlyt().zipWithNext().first { this == it.first }.second
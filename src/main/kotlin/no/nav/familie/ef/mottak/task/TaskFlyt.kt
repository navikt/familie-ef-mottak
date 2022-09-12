package no.nav.familie.ef.mottak.task

fun hovedflyt() = listOf(
    TaskType(LagPdfTask.TYPE),
    TaskType(ArkiverSøknadTask.TYPE),
    TaskType(VelgAutomatiskEllerManuellFlytTask.TYPE)
)

fun manuellJournalføringFlyt() = listOf(
    TaskType(LagJournalføringsoppgaveTask.TYPE),
    TaskType(PlasserOppgaveIMappeOppgaveTask.TYPE)
)

fun automatiskJournalføringFlyt() = listOf(
    TaskType(AutomatiskJournalførTask.TYPE)
)

fun ettersendingflyt() = listOf(
    TaskType(LagEttersendingPdfTask.TYPE),
    TaskType(ArkiverEttersendingTask.TYPE),
    TaskType(LagJournalføringsoppgaveForEttersendingTask.TYPE),
    TaskType(PlasserOppgaveIMappeOppgaveTask.TYPE)
)

fun eksternJournalføringFlyt() = listOf(
    TaskType(LagEksternJournalføringsoppgaveTask.TYPE)
)

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this == it.first }.second.type
fun TaskType.nesteManuellflytTask() = manuellJournalføringFlyt().zipWithNext().first { this == it.first }.second.type
fun TaskType.nesteEttersendingsflytTask() = ettersendingflyt().zipWithNext().first { this == it.first }.second.type

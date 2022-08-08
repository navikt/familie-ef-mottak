package no.nav.familie.ef.mottak.task

fun hovedflyt() = listOf(
    TaskType(LagPdfTask.TYPE),
    TaskType(ArkiverSøknadTask.TYPE),
    TaskType(LagJournalføringsoppgaveTask.TYPE),
    TaskType(PlasserOppgaveIMappeOppgaveTask.TYPE),
)

fun ettersendingflyt() = listOf(
    TaskType(LagEttersendingPdfTask.TYPE),
    TaskType(ArkiverEttersendingTask.TYPE),
    TaskType(LagJournalføringsoppgaveForEttersendingTask.TYPE),
    TaskType(PlasserOppgaveIMappeOppgaveTask.TYPE),
)

fun TaskType.nesteHovedflytTask() = hovedflyt().zipWithNext().first { this == it.first }.second.type
fun TaskType.nesteEttersendingsflytTask() = ettersendingflyt().zipWithNext().first { this == it.first }.second.type

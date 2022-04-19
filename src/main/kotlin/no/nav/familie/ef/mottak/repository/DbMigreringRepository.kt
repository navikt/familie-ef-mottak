package no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.repository.util.RepositoryInterface
import no.nav.familie.prosessering.domene.Task
import org.springframework.stereotype.Repository


@Repository
interface DbMigreringRepository : RepositoryInterface<Task, Long>

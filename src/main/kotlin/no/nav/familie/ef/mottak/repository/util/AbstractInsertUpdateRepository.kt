package no.nav.familie.ef.mottak.repository.util

import org.springframework.data.jdbc.core.JdbcAggregateOperations

abstract class AbstractInsertUpdateRepository<T : Any>(
    protected val entityOperations: JdbcAggregateOperations,
) {
    protected fun insertEntity(t: T): T = entityOperations.insert(t)

    protected fun updateEntity(t: T): T = entityOperations.update(t)
}

package no.nav.familie.ef.mottak.repository.util

interface InsertUpdateRepository<T> {
    fun insert(t: T): T

    fun insertAll(list: List<T>): List<T>

    fun update(t: T): T

    fun updateAll(list: List<T>): List<T>
}

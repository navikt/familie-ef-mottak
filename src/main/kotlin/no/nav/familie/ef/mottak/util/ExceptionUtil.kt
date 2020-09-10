package no.nav.familie.ef.mottak.util

import org.apache.commons.lang3.exception.ExceptionUtils

fun Throwable.getRootCause(): Throwable? = ExceptionUtils.getRootCause(this)
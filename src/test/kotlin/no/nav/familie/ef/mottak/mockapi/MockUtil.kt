package no.nav.familie.ef.mottak.mockapi

import io.mockk.clearMocks
import io.mockk.impl.annotations.MockK
import kotlin.reflect.full.declaredMemberProperties

/**
 * clearAllMocks i mockk resetter mocks tvers klasser og tvers spring tester
 */
fun clearAllMocks(instance: Any) {
    instance::class.declaredMemberProperties.forEach { property ->
        if (property.annotations.any { it.annotationClass == MockK::class }) {
            clearMocks(property.getter.call(instance)!!)
        }
    }
}

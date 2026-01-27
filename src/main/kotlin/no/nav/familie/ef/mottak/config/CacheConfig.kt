package no.nav.familie.ef.mottak.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    @Primary
    fun cacheManager(): CacheManager =
        CaffeineCacheManager().apply {
            isAllowNullValues = true

            setCaffeine(
                Caffeine
                    .newBuilder()
                    .maximumSize(1_000)
                    .expireAfterWrite(60, TimeUnit.MINUTES)
                    .recordStats(),
            )
        }
}

/**
 * Forventer treff, skal ikke brukes hvis en cache inneholder nullverdi
 * this.getCache(cache) burde aldri kunne returnere null, då den lager en cache hvis den ikke finnes fra før
 */
fun <K : Any, T> CacheManager.getValue(
    cache: String,
    key: K,
    valueLoader: () -> T,
): T = this.getNullable(cache, key, valueLoader) ?: error("Finner ikke cache for cache=$cache key=$key")

/**
 * Kan inneholde
 * this.getCache(cache) burde aldri kunne returnere null, då den lager en cache hvis den ikke finnes fra før
 */
fun <K : Any, T> CacheManager.getNullable(
    cache: String,
    key: K,
    valueLoader: () -> T?,
): T? {
    val cacheInstance = getCacheOrThrow(cache)
    val wrapper = cacheInstance.get(key)
    if (wrapper != null) {
        @Suppress("UNCHECKED_CAST")
        return wrapper.get() as? T
    }

    val loaded = valueLoader()
    cacheInstance.put(key, loaded)

    return loaded
}

fun CacheManager.getCacheOrThrow(cache: String) = this.getCache(cache) ?: error("Finner ikke cache=$cache")

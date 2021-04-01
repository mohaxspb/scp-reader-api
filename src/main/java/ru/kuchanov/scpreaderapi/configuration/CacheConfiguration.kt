package ru.kuchanov.scpreaderapi.configuration

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CacheConfiguration {

    @Bean
    fun provideCaffeine(): Caffeine<Any, Any> =
            Caffeine.newBuilder()
                    .recordStats()

    @Bean
    fun provideCacheManager(caffeine: Caffeine<Any, Any>): CacheManager =
            CaffeineCacheManager().apply {
                setCaffeine(caffeine)
            }
}

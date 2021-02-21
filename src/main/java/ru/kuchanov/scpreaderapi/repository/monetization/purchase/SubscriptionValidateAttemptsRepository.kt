package ru.kuchanov.scpreaderapi.repository.monetization.purchase

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.SubscriptionValidationAttempts

interface SubscriptionValidateAttemptsRepository : JpaRepository<SubscriptionValidationAttempts, Long> {

    @Query("""
        SELECT sva from SubscriptionValidationAttempts sva 
        WHERE sva.store = :store
        AND sva.attempts < :maxAttempts
    """)
    fun getAllByAttemptsBeforeAndStoreEquals(maxAttempts: Int, store: String): List<SubscriptionValidationAttempts>

    fun findFirstByStoreAndSubscriptionId(store: String, subscriptionId: Long): SubscriptionValidationAttempts?
}
package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscription

interface GoogleSubscriptionRepository : JpaRepository<GoogleSubscription, Long> {
    fun getOneById(id: Long): GoogleSubscription?
    fun getOneByPurchaseToken(purchaseToken: String): GoogleSubscription?
    fun findAllByOrderId(orderId: String): List<GoogleSubscription>
    fun findAllByLinkedPurchaseToken(linkedPurchaseToken: String): List<GoogleSubscription>

    @Query("""
        SELECT s from GoogleSubscription s 
            JOIN UserToGoogleSubscription uap ON s.id = uap.googleSubscriptionId 
            WHERE uap.userId = :userId
    """)
    fun getAllByUserId(userId: Long): List<GoogleSubscription>
}

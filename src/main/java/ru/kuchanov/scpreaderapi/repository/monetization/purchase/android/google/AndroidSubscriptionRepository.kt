package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscription

interface AndroidSubscriptionRepository : JpaRepository<GoogleSubscription, Long> {
    fun getOneById(id: Long): GoogleSubscription?
    fun getOneByPurchaseToken(purchaseToken: String): GoogleSubscription?
    fun getOneByOrderId(orderId: String): GoogleSubscription?
    fun getOneByLinkedPurchaseToken(linkedPurchaseToken: String): GoogleSubscription?
}

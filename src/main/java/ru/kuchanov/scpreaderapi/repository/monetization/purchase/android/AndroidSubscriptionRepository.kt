package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription

interface AndroidSubscriptionRepository : JpaRepository<AndroidSubscription, Long> {
    fun getOneById(id: Long): AndroidSubscription?
    fun getOneByPurchaseToken(purchaseToken: String): AndroidSubscription?
    fun getOneByOrderId(orderId: String): AndroidSubscription?
}

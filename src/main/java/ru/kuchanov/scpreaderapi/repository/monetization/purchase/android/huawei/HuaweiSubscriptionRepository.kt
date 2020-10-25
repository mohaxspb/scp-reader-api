package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription

interface HuaweiSubscriptionRepository : JpaRepository<HuaweiSubscription, Long> {
    fun getOneById(id: Long): HuaweiSubscription?
    fun getOneByPurchaseToken(purchaseToken: String): HuaweiSubscription?
    fun getOneByOrderId(orderId: String): HuaweiSubscription?
    fun getOneBySubscriptionId(subscriptionId: String): HuaweiSubscription?
}

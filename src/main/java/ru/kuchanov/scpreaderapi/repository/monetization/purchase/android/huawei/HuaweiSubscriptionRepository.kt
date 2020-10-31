package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription

interface HuaweiSubscriptionRepository : JpaRepository<HuaweiSubscription, Long> {
    fun getOneById(id: Long): HuaweiSubscription?
    fun getOneBySubscriptionId(subscriptionId: String): HuaweiSubscription?

    @Query("""
        SELECT s from HuaweiSubscription s 
            JOIN UserToHuaweiSubscription uap ON s.id = uap.huaweiSubscriptionId 
            WHERE uap.userId = :userId
    """)
    fun getHuaweiSubscriptionsByUserId(userId: Long): List<HuaweiSubscription>
}

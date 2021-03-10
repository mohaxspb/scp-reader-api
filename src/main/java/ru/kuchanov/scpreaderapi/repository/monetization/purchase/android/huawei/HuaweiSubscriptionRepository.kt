package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import java.time.LocalDateTime

interface HuaweiSubscriptionRepository : JpaRepository<HuaweiSubscription, Long> {
    fun getOneById(id: Long): HuaweiSubscription?

    @Query("""
        SELECT s from HuaweiSubscription s 
            JOIN UserToHuaweiSubscription uap ON s.id = uap.huaweiSubscriptionId 
            WHERE uap.userId = :userId
    """)
    fun getHuaweiSubscriptionsByUserId(userId: Long): List<HuaweiSubscription>

    @Suppress("JpaQlInspection") //seems to be this must work
    @Query("""
        SELECT s from HuaweiSubscription s 
            WHERE s.expiryTimeMillis BETWEEN :startDate AND :endDate
    """)
    fun getHuaweiSubscriptionsBetweenDates(
            startDate: LocalDateTime,
            endDate: LocalDateTime
    ): List<HuaweiSubscription>

    fun getHuaweiSubscriptionBySubscriptionId(subscriptionId: String): HuaweiSubscription?

    fun getHuaweiSubscriptionByOriSubscriptionId(oriSubscriptionId: String): HuaweiSubscription?
}

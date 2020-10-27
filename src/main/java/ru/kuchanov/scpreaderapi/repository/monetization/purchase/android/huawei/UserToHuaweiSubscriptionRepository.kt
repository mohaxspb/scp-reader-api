package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.UserToHuaweiSubscription

interface UserToHuaweiSubscriptionRepository : JpaRepository<UserToHuaweiSubscription, Long> {

    @Query("""
        SELECT s from HuaweiSubscription s 
            JOIN UserToHuaweiSubscription uap ON s.id = uap.huaweiSubscriptionId 
            WHERE uap.userId = :userId
    """)
    fun getAndroidProductsByUserId(userId: Long): List<UserToHuaweiSubscription>
}
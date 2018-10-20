package ru.kuchanov.scpreaderapi.repository.purchase

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription

interface UserAndroidSubscriptionRepository : JpaRepository<UsersAndroidSubscription, Long> {

    @Query("SELECT s from AndroidSubscription s " +
            "JOIN UsersAndroidSubscription uas ON s.id = uas.androidSubscriptionId " +
            "WHERE uas.userId = :userId")
    fun getAndroidSubscriptionByUserId(userId: Long): List<AndroidSubscription>
}

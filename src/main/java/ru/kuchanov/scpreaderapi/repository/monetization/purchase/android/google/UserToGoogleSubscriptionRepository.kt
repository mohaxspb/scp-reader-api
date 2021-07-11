package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.google.UserToGoogleSubscription

interface UserToGoogleSubscriptionRepository : JpaRepository<UserToGoogleSubscription, Long> {

    fun findByGoogleSubscriptionIdAndUserId(subscriptionId: Long, userId: Long): UserToGoogleSubscription?
}
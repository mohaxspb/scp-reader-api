package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.google.UserToGoogleSubscription
import ru.kuchanov.scpreaderapi.bean.users.User

interface UserToGoogleSubscriptionRepository : JpaRepository<UserToGoogleSubscription, Long> {

    fun findByGoogleSubscriptionIdAndUserId(subscriptionId: Long, userId: Long): UserToGoogleSubscription?

    @Query("""
        select distinct u from User u 
        join UserToGoogleSubscription uap on u.id = uap.userId 
        where uap.googleSubscriptionId = :googleSubscriptionId
    """)
    fun getUserByGoogleSubscriptionId(googleSubscriptionId: Long): User?
}
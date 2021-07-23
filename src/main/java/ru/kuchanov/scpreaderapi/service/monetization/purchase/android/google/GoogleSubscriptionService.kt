package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import javax.transaction.Transactional

interface GoogleSubscriptionService {

    fun getById(id: Long): GoogleSubscription?
    fun findAllByPurchaseToken(purchaseToken: String): List<GoogleSubscription>

    fun save(googleProduct: GoogleSubscription): GoogleSubscription
    fun saveAll(subscriptions: List<GoogleSubscription>): List<GoogleSubscription>

    fun deleteById(id: Long)

    @Transactional
    fun saveSubscription(
        subscriptionPurchase: SubscriptionPurchase,
        purchaseToken: String,
        sku: String,
        user: User
    ): GoogleSubscription

    fun getUserByGoogleSubscriptionId(googleSubscriptionId: Long): User?

    fun getGoogleSubscriptionsForUser(userId: Long): List<GoogleSubscription>
}

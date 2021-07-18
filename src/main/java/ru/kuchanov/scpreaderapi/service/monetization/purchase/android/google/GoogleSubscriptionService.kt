package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscription
import ru.kuchanov.scpreaderapi.bean.users.User

interface GoogleSubscriptionService {

    fun getById(id: Long): GoogleSubscription?
    fun getByPurchaseToken(purchaseToken: String): GoogleSubscription?
    fun getByOrderId(orderId: String): GoogleSubscription?

    fun save(googleProduct: GoogleSubscription): GoogleSubscription
    fun saveAll(subscriptions: List<GoogleSubscription>): List<GoogleSubscription>

    fun deleteById(id: Long)

    fun saveSubscription(
        subscriptionPurchase: SubscriptionPurchase,
        purchaseToken: String,
        user: User
    ): GoogleSubscription

    fun getUserByGoogleSubscriptionId(googleSubscriptionId: Long): User?
}

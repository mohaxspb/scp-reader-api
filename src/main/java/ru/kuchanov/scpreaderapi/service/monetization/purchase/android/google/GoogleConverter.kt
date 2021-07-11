package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscription
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class GoogleConverter @Autowired constructor(
    @Value("\${monetization.subscriptions.google.packageName}") private val googlePackageName: String
) {

    fun convert(subscription: SubscriptionPurchase, purchaseToken: String): GoogleSubscription =
        GoogleSubscription(
            androidPackage = googlePackageName,
            autoRenewing = subscription.autoRenewing,
            expiryTimeMillis = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(subscription.expiryTimeMillis),
                ZoneOffset.UTC
            ),
            linkedPurchaseToken = subscription.linkedPurchaseToken,
            priceAmountMicros = subscription.priceAmountMicros,
            orderId = subscription.orderId,
            priceCurrencyCode = subscription.priceCurrencyCode,
            purchaseToken = purchaseToken,
            startTimeMillis = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(subscription.startTimeMillis),
                ZoneOffset.UTC
            ),
            userCancellationTimeMillis = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(subscription.userCancellationTimeMillis),
                ZoneOffset.UTC
            )
        )
}
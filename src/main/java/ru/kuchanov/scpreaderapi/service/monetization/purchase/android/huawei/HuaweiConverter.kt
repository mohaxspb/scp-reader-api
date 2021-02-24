package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.model.huawei.purchase.InAppPurchaseData
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class HuaweiConverter {

    fun convertToSubscription(inAppPurchaseData: InAppPurchaseData): HuaweiSubscription =
            with(inAppPurchaseData) {
                HuaweiSubscription(
                        androidPackage = packageName!!,
                        accountFlag = accountFlag,
                        autoRenewing = inAppPurchaseData.autoRenewing,
                        country = country,
                        //next renew date
                        expiryTimeMillis = LocalDateTime.ofInstant(Instant.ofEpochMilli(expirationDate!!), ZoneOffset.UTC),
                        kind = kind,
                        orderId = orderId,
                        oriSubscriptionId = oriSubscriptionId,
                        price = price,
                        priceCurrencyCode = currency,
                        purchaseState = purchaseState,
                        payOrderId = payOrderId!!,
                        purchaseTime = purchaseTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) },
                        productName = productName,
                        productId = productId,
                        productGroup = productGroup!!,
                        subscriptionId = subscriptionId!!,
                        //subscription renewal stops
                        userCancellationTimeMillis = cancellationTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) },
                        //refunded
                        cancelTime = cancelTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) },
                        purchaseToken = purchaseToken,
                        subIsValid = subIsvalid!!,
                        startTimeMillis = purchaseTime?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
                )
            }
}
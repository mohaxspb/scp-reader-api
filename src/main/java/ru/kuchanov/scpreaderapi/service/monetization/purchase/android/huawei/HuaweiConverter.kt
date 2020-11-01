package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.model.huawei.purchase.InAppPurchaseData
import java.sql.Timestamp

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
                        expiryTimeMillis = Timestamp(expirationDate!!),
                        kind = kind,
                        orderId = orderId,
                        oriSubscriptionId = oriSubscriptionId,
                        price = price,
                        priceCurrencyCode = currency,
                        purchaseState = purchaseState,
                        payOrderId = payOrderId!!,
                        purchaseTime = purchaseTime?.let { Timestamp(it) },
                        productName = productName,
                        productId = productId,
                        productGroup = productGroup!!,
                        subscriptionId = subscriptionId!!,
                        //subscription renewal stops
                        userCancellationTimeMillis = cancellationTime?.let { Timestamp(it) },
                        //refunded
                        cancelTime = cancelTime?.let { Timestamp(it) },
                        purchaseToken = purchaseToken,
                        subIsValid = subIsvalid!!,
                        startTimeMillis = purchaseTime?.let { Timestamp(it) }
                )
            }
}
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
                        accountFlag = accountFlag!!,
                        autoRenewing = inAppPurchaseData.autoRenewing,
                        country = country,
                        expiryTimeMillis = Timestamp(expirationDate!!),
                        kind = kind,
                        orderId = orderId,
                        oriSubscriptionId = oriSubscriptionId,
                        price = price,
                        priceCurrencyCode = currency,
                        purchaseState = purchaseState,
                        productName = productName,
                        productId = productId,
                        productGroup = productGroup!!,
                        subscriptionId = subscriptionId!!,
                        userCancellationTimeMillis = Timestamp(cancelTime!!),
                        purchaseToken = purchaseToken,
                        subIsValid = subIsvalid!!,
                        startTimeMillis = Timestamp(purchaseTime!!)
                )
            }
}
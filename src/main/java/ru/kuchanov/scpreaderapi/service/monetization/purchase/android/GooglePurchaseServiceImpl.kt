package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import javax.servlet.http.HttpServletResponse

@Service
class GooglePurchaseServiceImpl : AndroidPurchaseService {

    @Autowired
    private lateinit var androidPublisher: AndroidPublisher

    override fun validateProductPurchase(packageName: String, sku: String, purchaseToken: String): ValidationResponse {
        val productRequest: AndroidPublisher.Purchases.Products.Get = androidPublisher
                .purchases()
                .products()
                .get(
                        packageName,
                        sku,
                        purchaseToken
                )

        return try {
            val productPurchase = productRequest.execute()
            ValidationResponse.AndroidProductResponse(ValidationStatus.VALID, productPurchase)
        } catch (e: GoogleJsonResponseException) {
            println("Error while validate product: $e")
            ValidationResponse.AndroidProductResponse(
                    if (e.details.code == HttpServletResponse.SC_BAD_REQUEST) {
                        ValidationStatus.INVALID
                    } else {
                        ValidationStatus.SERVER_ERROR
                    },
                    null
            )
        }
    }

    override fun validateSubscriptionPurchase(packageName: String, sku: String, purchaseToken: String): ValidationResponse {
        val subscriptionRequest: AndroidPublisher.Purchases.Subscriptions.Get = androidPublisher
                .purchases()
                .subscriptions()
                .get(
                        packageName,
                        sku,
                        purchaseToken
                )

        return try {
            val subscription: SubscriptionPurchase = subscriptionRequest.execute()
            ValidationResponse.AndroidSubscriptionResponse(ValidationStatus.VALID, subscription)
        } catch (e: GoogleJsonResponseException) {
            println("Error while validate subscription: $e")
            ValidationResponse.AndroidSubscriptionResponse(
                    if (e.details.code == HttpServletResponse.SC_BAD_REQUEST) {
                        ValidationStatus.INVALID
                    } else {
                        ValidationStatus.SERVER_ERROR
                    },
                    null
            )
        }
    }
}
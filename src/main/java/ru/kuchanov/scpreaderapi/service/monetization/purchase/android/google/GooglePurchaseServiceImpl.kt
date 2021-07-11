package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import com.google.api.services.androidpublisher.model.SubscriptionPurchasesAcknowledgeRequest
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.model.dto.purchase.GoogleAcknowledgeResult
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationResponse
import ru.kuchanov.scpreaderapi.model.dto.purchase.ValidationStatus
import javax.servlet.http.HttpServletResponse

@Service
class GooglePurchaseServiceImpl @Autowired constructor(
    private val androidPublisher: AndroidPublisher,
    @Qualifier(Application.GOOGLE_LOGGER) private val googleLog: Logger,
    @Value("\${monetization.subscriptions.google.packageName}") private val googlePackageName: String
) : GooglePurchaseService {

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
        } catch (e: Throwable) {
            googleLog.error("Error while validate product: $e", e)
            ValidationResponse.AndroidProductResponse(
                if (e is GoogleJsonResponseException && e.details.code == HttpServletResponse.SC_BAD_REQUEST) {
                    ValidationStatus.INVALID
                } else {
                    ValidationStatus.SERVER_ERROR
                },
                null
            )
        }
    }

    override fun validateSubscriptionPurchase(
        packageName: String,
        sku: String,
        purchaseToken: String
    ): ValidationResponse {
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
            ValidationResponse.GoogleSubscriptionResponse(ValidationStatus.VALID, subscription)
        } catch (e: Throwable) {
            googleLog.error("Error while validate subscription: $e", e)
            ValidationResponse.GoogleSubscriptionResponse(
                if (e is GoogleJsonResponseException && e.details.code == HttpServletResponse.SC_BAD_REQUEST) {
                    ValidationStatus.INVALID
                } else {
                    ValidationStatus.SERVER_ERROR
                },
                null
            )
        }
    }

    /**
     * @param subscriptionId aka SKU
     */
    override fun acknowledgeSubscription(
        subscriptionId: String,
        purchaseToken: String
    ): GoogleAcknowledgeResult {
        val content = SubscriptionPurchasesAcknowledgeRequest()
        val acknowledgeRequest: AndroidPublisher.Purchases.Subscriptions.Acknowledge =
            androidPublisher.purchases().subscriptions().acknowledge(
                googlePackageName,
                subscriptionId,
                purchaseToken,
                content
            )

        return try {
            acknowledgeRequest.execute()
            GoogleAcknowledgeResult.GoogleSubscriptionAcknowledgeSuccess()
        } catch (e: Throwable) {
            googleLog.error("Error while acknowledge subscription: $subscriptionId, $purchaseToken", e)
            GoogleAcknowledgeResult.GoogleSubscriptionAcknowledgeFailure(e)
        }
    }
}
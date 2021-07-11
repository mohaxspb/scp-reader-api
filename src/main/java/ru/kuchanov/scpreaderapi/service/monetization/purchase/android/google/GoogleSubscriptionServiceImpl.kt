package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import com.google.api.services.androidpublisher.model.SubscriptionPurchase
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.google.UserToGoogleSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google.AndroidSubscriptionRepository
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google.UserToGoogleSubscriptionRepository

@Service
class GoogleSubscriptionServiceImpl @Autowired constructor(
    private val androidSubscriptionRepository: AndroidSubscriptionRepository,
    private val userToGoogleSubscriptionRepository: UserToGoogleSubscriptionRepository,
    private val googleConverter: GoogleConverter,
    @Qualifier(Application.GOOGLE_LOGGER) private val log: Logger
) : GoogleSubscriptionService {

    override fun getById(id: Long) =
        androidSubscriptionRepository.getOneById(id)

    override fun getByPurchaseToken(purchaseToken: String): GoogleSubscription? =
        androidSubscriptionRepository.getOneByPurchaseToken(purchaseToken)

    override fun getByOrderId(orderId: String): GoogleSubscription? =
        androidSubscriptionRepository.getOneByOrderId(orderId)

    override fun saveAll(subscriptions: List<GoogleSubscription>): List<GoogleSubscription> =
        androidSubscriptionRepository.saveAll(subscriptions)

    override fun save(googleProduct: GoogleSubscription): GoogleSubscription =
        androidSubscriptionRepository.save(googleProduct)

    override fun deleteById(id: Long) =
        androidSubscriptionRepository.deleteById(id)

    override fun saveSubscription(
        subscriptionPurchase: SubscriptionPurchase,
        purchaseToken: String,
        user: User
    ): GoogleSubscription {
        val googleSubscription: GoogleSubscription = googleConverter.convert(subscriptionPurchase, purchaseToken)
        val googleSubscriptionInDb = androidSubscriptionRepository
            .getOneByOrderId(orderId = googleSubscription.orderId)

        log.info("googleSubscription: $googleSubscription")

        log.info("googleSubscriptionInDb: $googleSubscriptionInDb")

        val oriSubscriptionInDb = googleSubscription.linkedPurchaseToken?.let {
            androidSubscriptionRepository.getOneByLinkedPurchaseToken(it)
        }
        log.info("Subscription has ORI subscription: $oriSubscriptionInDb")
        val subscriptionUpdated = if (googleSubscriptionInDb != null) {
            androidSubscriptionRepository.save(googleSubscription.copy(id = googleSubscriptionInDb.id))
        } else {
            androidSubscriptionRepository.save(googleSubscription)
        }

        checkNotNull(subscriptionUpdated.id)
        checkNotNull(user.id)

        val userToSubscriptionConnectionInDb = userToGoogleSubscriptionRepository
            .findByGoogleSubscriptionIdAndUserId(subscriptionId = subscriptionUpdated.id, userId = user.id)
        val userToSubscriptionConnectionToSaveOrUpdate = if (userToSubscriptionConnectionInDb == null) {
            UserToGoogleSubscription(googleSubscriptionId = subscriptionUpdated.id, userId = user.id)
        } else {
            UserToGoogleSubscription(
                id = userToSubscriptionConnectionInDb.id,
                googleSubscriptionId = subscriptionUpdated.id,
                userId = user.id
            )
        }

        userToGoogleSubscriptionRepository.save(userToSubscriptionConnectionToSaveOrUpdate)

        return subscriptionUpdated
    }
}

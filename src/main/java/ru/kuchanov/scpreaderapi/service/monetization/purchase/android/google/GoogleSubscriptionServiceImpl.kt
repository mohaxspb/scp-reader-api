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
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google.GoogleSubscriptionRepository
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google.UserToGoogleSubscriptionRepository

@Service
class GoogleSubscriptionServiceImpl @Autowired constructor(
    private val googleSubscriptionRepository: GoogleSubscriptionRepository,
    private val userToGoogleSubscriptionRepository: UserToGoogleSubscriptionRepository,
    private val googleConverter: GoogleConverter,
    @Qualifier(Application.GOOGLE_LOGGER) private val log: Logger
) : GoogleSubscriptionService {

    override fun getById(id: Long) =
        googleSubscriptionRepository.getOneById(id)

    override fun getByPurchaseToken(purchaseToken: String): GoogleSubscription? =
        googleSubscriptionRepository.getOneByPurchaseToken(purchaseToken)

    override fun getByOrderId(orderId: String): GoogleSubscription? =
        googleSubscriptionRepository.getOneByOrderId(orderId)

    override fun saveAll(subscriptions: List<GoogleSubscription>): List<GoogleSubscription> =
        googleSubscriptionRepository.saveAll(subscriptions)

    override fun save(googleProduct: GoogleSubscription): GoogleSubscription =
        googleSubscriptionRepository.save(googleProduct)

    override fun deleteById(id: Long) =
        googleSubscriptionRepository.deleteById(id)

    override fun saveSubscription(
        subscriptionPurchase: SubscriptionPurchase,
        purchaseToken: String,
        user: User
    ): GoogleSubscription {
        val googleSubscription: GoogleSubscription = googleConverter.convert(subscriptionPurchase, purchaseToken)
        val googleSubscriptionInDb = googleSubscriptionRepository
            .getOneByOrderId(orderId = googleSubscription.orderId)

        log.info("googleSubscription: $googleSubscription")

        log.info("googleSubscriptionInDb: $googleSubscriptionInDb")

        val oriSubscriptionInDb = googleSubscription.linkedPurchaseToken?.let {
            googleSubscriptionRepository.getOneByLinkedPurchaseToken(it)
        }
        log.info("Subscription has ORI subscription: $oriSubscriptionInDb")
        val subscriptionUpdated = if (googleSubscriptionInDb != null) {
            googleSubscriptionRepository.save(googleSubscription.copy(id = googleSubscriptionInDb.id))
        } else {
            googleSubscriptionRepository.save(googleSubscription)
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

    override fun getUserByGoogleSubscriptionId(googleSubscriptionId: Long): User? =
        userToGoogleSubscriptionRepository.getUserByGoogleSubscriptionId(googleSubscriptionId)

    override fun getGoogleSubscriptionsForUser(userId: Long): List<GoogleSubscription> =
        googleSubscriptionRepository.getAllByUserId(userId)
}

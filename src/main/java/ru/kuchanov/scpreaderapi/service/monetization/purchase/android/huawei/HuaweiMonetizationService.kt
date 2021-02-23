package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiProduct
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.UserToHuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.huawei.purchase.InAppPurchaseData
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei.HuaweiSubscriptionRepository
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei.UserToHuaweiSubscriptionRepository
import ru.kuchanov.scpreaderapi.repository.users.UsersRepository
import java.time.LocalDateTime

@Service
class HuaweiMonetizationService @Autowired constructor(
        private val huaweiSubscriptionRepository: HuaweiSubscriptionRepository,
        private val userToHuaweiSubscriptionRepository: UserToHuaweiSubscriptionRepository,
        private val userRepository: UsersRepository,
        private val converter: HuaweiConverter,
        private val log: Logger
) {

    fun getHuaweiSubscriptionsForUser(userId: Long): List<HuaweiSubscription> =
            huaweiSubscriptionRepository.getHuaweiSubscriptionsByUserId(userId)

    fun getUserByHuaweiSubscriptionId(huaweiSubscriptionId: Long): User? =
            userRepository.getUserByHuaweiSubscriptionId(huaweiSubscriptionId)

    fun getHuaweiSubscriptionsBetweenDates(
            startDate: LocalDateTime,
            endDate: LocalDateTime
    ): List<HuaweiSubscription> =
            huaweiSubscriptionRepository.getHuaweiSubscriptionsBetweenDates(startDate, endDate)

//    fun savePurchasedProduct(inAppPurchaseData: InAppPurchaseData, user: User) {
//        when (HuaweiProductKind.findByType(inAppPurchaseData.kind)) {
//            HuaweiProductKind.CONSUMABLE, HuaweiProductKind.NON_CONSUMABLE -> saveProduct(TODO(), user)
//            HuaweiProductKind.RENEWABLE_SUBSCRIPTION, HuaweiProductKind.NON_RENEWABLE_SUBSCRIPTION -> {
//                saveSubscription(converter.convertToSubscription(inAppPurchaseData), user)
//            }
//        }
//    }

    fun saveSubscription(inAppPurchaseData: InAppPurchaseData, user: User): HuaweiSubscription {
        val huaweiSubscription = converter.convertToSubscription(inAppPurchaseData)
        val subscription = huaweiSubscriptionRepository.save(huaweiSubscription)

        val userToSubscriptionConnection = UserToHuaweiSubscription(
                huaweiSubscriptionId = subscription.id!!,
                userId = user.id!!
        )
        userToHuaweiSubscriptionRepository.save(userToSubscriptionConnection)

        return subscription
    }

    private fun saveProduct(huaweiProduct: HuaweiProduct, user: User) {
        TODO()
    }
}
package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiProduct
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.UserToHuaweiSubscription
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.huawei.purchase.HuaweiProductKind
import ru.kuchanov.scpreaderapi.model.huawei.purchase.InAppPurchaseData
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei.HuaweiSubscriptionRepository
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei.UserToHuaweiSubscriptionRepository

@Service
class HuaweiMonetizationService @Autowired constructor(
        private val huaweiSubscriptionRepository: HuaweiSubscriptionRepository,
        private val userToHuaweiSubscriptionRepository: UserToHuaweiSubscriptionRepository,
        private val converter: HuaweiConverter
) {

    fun getHuaweiSubscriptionsForUser(userId: Long): List<HuaweiSubscription> =
            huaweiSubscriptionRepository.getHuaweiSubscriptionsByUserId(userId)

    fun savePurchasedProduct(inAppPurchaseData: InAppPurchaseData, user: User) {
        when (HuaweiProductKind.findByType(inAppPurchaseData.kind)) {
            HuaweiProductKind.CONSUMABLE, HuaweiProductKind.NON_CONSUMABLE -> saveProduct(TODO(), user)
            HuaweiProductKind.RENEWABLE_SUBSCRIPTION, HuaweiProductKind.NON_RENEWABLE_SUBSCRIPTION -> {
                saveSubscription(converter.convertToSubscription(inAppPurchaseData), user)
            }
        }
    }

    private fun saveSubscription(huaweiSubscription: HuaweiSubscription, user: User) {
        val subscription = huaweiSubscriptionRepository.save(huaweiSubscription)

        val userToSubscriptionConnection = UserToHuaweiSubscription(
                huaweiSubscriptionId = subscription.id!!,
                userId = user.id!!
        )
        userToHuaweiSubscriptionRepository.save(userToSubscriptionConnection)
    }

    private fun saveProduct(huaweiProduct: HuaweiProduct, user: User) {
        TODO()
    }
}
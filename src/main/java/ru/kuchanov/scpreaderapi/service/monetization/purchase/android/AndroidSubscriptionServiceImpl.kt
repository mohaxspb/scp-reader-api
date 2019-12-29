package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.AndroidSubscriptionRepository

@Service
class AndroidSubscriptionServiceImpl @Autowired constructor(
        val androidSubscriptionRepository: AndroidSubscriptionRepository
) : AndroidSubscriptionService {

    override fun getById(id: Long) = androidSubscriptionRepository.getOneById(id)

    override fun getByPurchaseToken(purchaseToken: String): AndroidSubscription? =
            androidSubscriptionRepository.getOneByPurchaseToken(purchaseToken)

    override fun getByOrderId(orderId: String): AndroidSubscription? =
            androidSubscriptionRepository.getOneByOrderId(orderId)

    override fun saveAll(subscriptions: List<AndroidSubscription>): List<AndroidSubscription> =
            androidSubscriptionRepository.saveAll(subscriptions)

    override fun save(androidProduct: AndroidSubscription): AndroidSubscription = androidSubscriptionRepository.save(androidProduct)

    override fun deleteById(id: Long) = androidSubscriptionRepository.deleteById(id)
}

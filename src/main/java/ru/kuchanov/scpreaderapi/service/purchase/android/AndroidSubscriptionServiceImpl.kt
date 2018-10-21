package ru.kuchanov.scpreaderapi.service.purchase.android

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.repository.purchase.android.AndroidSubscriptionRepository

@Service
class AndroidSubscriptionServiceImpl : AndroidSubscriptionService {

    @Autowired
    private lateinit var androidSubscriptionRepository: AndroidSubscriptionRepository

    override fun getById(id: Long) = androidSubscriptionRepository.getOneById(id)

    override fun getByPurchaseToken(purchaseToken: String): AndroidSubscription? =
            androidSubscriptionRepository.getOneByPurchaseToken(purchaseToken)

    override fun getByOrderId(orderId: String): AndroidSubscription? =
            androidSubscriptionRepository.getOneByOrderId(orderId)

    override fun findAll(): List<AndroidSubscription> = androidSubscriptionRepository.findAll()

    override fun saveAll(subscriptions: List<AndroidSubscription>): List<AndroidSubscription> =
            androidSubscriptionRepository.saveAll(subscriptions)

    override fun save(androidProduct: AndroidSubscription): AndroidSubscription = androidSubscriptionRepository.save(androidProduct)

    override fun deleteById(id: Long) = androidSubscriptionRepository.deleteById(id)
}
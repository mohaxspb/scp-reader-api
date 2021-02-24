package ru.kuchanov.scpreaderapi.service.monetization.purchase

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.SubscriptionValidationAttempts
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.SubscriptionValidateAttemptsRepository

@Service
class SubscriptionValidateAttemptsService @Autowired constructor(
        private val subscriptionValidateAttemptsRepository: SubscriptionValidateAttemptsRepository
) {
    fun getByStoreAndSubscriptionId(store: String, subscriptionId: Long): SubscriptionValidationAttempts? =
            subscriptionValidateAttemptsRepository.findFirstByStoreAndSubscriptionId(store, subscriptionId)

    fun save(subscriptionValidateAttempts: SubscriptionValidationAttempts): SubscriptionValidationAttempts =
            subscriptionValidateAttemptsRepository.save(subscriptionValidateAttempts)!!
}
package ru.kuchanov.scpreaderapi.service.purchase.android

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription
import ru.kuchanov.scpreaderapi.repository.purchase.android.UserAndroidProductRepository
import ru.kuchanov.scpreaderapi.repository.purchase.android.UserAndroidSubscriptionRepository

@Service
class UserAndroidPurchaseServerImpl : UserAndroidPurchaseService {

    @Autowired
    private lateinit var userAndroidProductRepository: UserAndroidProductRepository

    @Autowired
    private lateinit var userAndroidSubscriptionRepository: UserAndroidSubscriptionRepository

    override fun getByUserIdAndAndroidProductId(userId: Long, androidProductId: Long): UsersAndroidProduct? =
            userAndroidProductRepository.getOneByUserIdAndAndroidProductId(userId, androidProductId)

    override fun getByUserIdAndAndroidSubscriptionId(userId: Long, androidSubscriptionId: Long): UsersAndroidSubscription? =
            userAndroidSubscriptionRepository.getOneByUserIdAndAndroidSubscriptionId(userId, androidSubscriptionId)

    override fun save(usersAndroidSubscription: UsersAndroidSubscription): UsersAndroidSubscription =
            userAndroidSubscriptionRepository.save(usersAndroidSubscription)

    override fun save(usersAndroidProduct: UsersAndroidProduct): UsersAndroidProduct =
            userAndroidProductRepository.save(usersAndroidProduct)

    override fun findAllProducts(userId: Long): List<AndroidProduct> =
            userAndroidProductRepository.getAndroidProductsByUserId(userId)

    override fun findAllSubscriptions(userId: Long): List<AndroidSubscription> =
            userAndroidSubscriptionRepository.getAndroidSubscriptionByUserId(userId)
}
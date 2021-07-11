package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.UserAndroidProductRepository

@Service
class UserAndroidPurchaseServerImpl @Autowired constructor(
    private val userAndroidProductRepository: UserAndroidProductRepository,
) : UserAndroidPurchaseService {

    override fun getByUserIdAndAndroidProductId(userId: Long, androidProductId: Long): UsersAndroidProduct? =
        userAndroidProductRepository.getOneByUserIdAndAndroidProductId(userId, androidProductId)

    override fun save(usersAndroidProduct: UsersAndroidProduct): UsersAndroidProduct =
        userAndroidProductRepository.save(usersAndroidProduct)

    override fun findAllProducts(userId: Long): List<AndroidProduct> =
        userAndroidProductRepository.getAndroidProductsByUserId(userId)
}
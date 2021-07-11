package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct

interface UserAndroidPurchaseService {

    fun save(usersAndroidProduct: UsersAndroidProduct): UsersAndroidProduct

    fun findAllProducts(userId: Long): List<AndroidProduct>

    fun getByUserIdAndAndroidProductId(userId: Long, androidProductId: Long): UsersAndroidProduct?
}
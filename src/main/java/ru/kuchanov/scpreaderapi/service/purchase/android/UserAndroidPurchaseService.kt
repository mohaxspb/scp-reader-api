package ru.kuchanov.scpreaderapi.service.purchase.android

import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription

interface UserAndroidPurchaseService {

    fun save(usersAndroidSubscription: UsersAndroidSubscription): UsersAndroidSubscription
    fun save(usersAndroidProduct: UsersAndroidProduct): UsersAndroidProduct
    fun findAllProducts(userId: Long): List<AndroidProduct>
    fun findAllSubscriptions(userId: Long): List<AndroidSubscription>
}
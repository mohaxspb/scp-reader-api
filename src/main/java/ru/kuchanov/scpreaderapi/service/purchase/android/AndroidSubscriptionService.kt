package ru.kuchanov.scpreaderapi.service.purchase.android

import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription

interface AndroidSubscriptionService {

    fun getById(id: Long): AndroidSubscription?
    fun getByPurchaseToken(purchaseToken: String): AndroidSubscription?
    fun getByOrderId(orderId: String): AndroidSubscription?

    fun findAll(): List<AndroidSubscription>

    fun saveAll(subscriptions: List<AndroidSubscription>): List<AndroidSubscription>
    fun save(androidProduct: AndroidSubscription): AndroidSubscription

    fun deleteById(id: Long)
}
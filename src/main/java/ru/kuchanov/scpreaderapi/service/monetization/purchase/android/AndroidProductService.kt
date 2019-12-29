package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct

interface AndroidProductService {

    fun getById(id: Long): AndroidProduct?
    fun getByPurchaseToken(purchaseToken: String): AndroidProduct?
    fun getByOrderId(orderId: String): AndroidProduct?

    fun save(androidProduct: AndroidProduct): AndroidProduct
    fun saveAll(androidProducts: List<AndroidProduct>): List<AndroidProduct>

    fun deleteById(id: Long)
}

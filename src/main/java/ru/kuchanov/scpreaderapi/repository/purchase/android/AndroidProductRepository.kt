package ru.kuchanov.scpreaderapi.repository.purchase.android

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct

interface AndroidProductRepository : JpaRepository<AndroidProduct, Long> {
    fun getOneById(id: Long): AndroidProduct?
    fun getOneByPurchaseToken(purchaseToken: String): AndroidProduct?
    fun getOneByOrderId(orderId: String): AndroidProduct?
}

package ru.kuchanov.scpreaderapi.service.monetization.purchase.android

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.AndroidProductRepository

@Service
class AndroidProductServiceImpl @Autowired constructor(
        val androidProductRepository: AndroidProductRepository
) : AndroidProductService {

    override fun getById(id: Long) = androidProductRepository.getOneById(id)

    override fun getByPurchaseToken(purchaseToken: String): AndroidProduct? =
            androidProductRepository.getOneByPurchaseToken(purchaseToken)

    override fun getByOrderId(orderId: String): AndroidProduct? =
            androidProductRepository.getOneByOrderId(orderId)

    override fun saveAll(androidProducts: List<AndroidProduct>): List<AndroidProduct> =
            androidProductRepository.saveAll(androidProducts)

    override fun save(androidProduct: AndroidProduct): AndroidProduct = androidProductRepository.save(androidProduct)

    override fun deleteById(id: Long) = androidProductRepository.deleteById(id)
}

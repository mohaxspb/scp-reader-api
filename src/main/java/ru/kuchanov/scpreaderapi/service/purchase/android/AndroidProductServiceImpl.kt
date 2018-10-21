package ru.kuchanov.scpreaderapi.service.purchase.android

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.repository.purchase.android.AndroidProductRepository

@Service
class AndroidProductServiceImpl : AndroidProductService {

    @Autowired
    private lateinit var androidProductRepository: AndroidProductRepository

    override fun getById(id: Long) = androidProductRepository.getOneById(id)

    override fun findAll(): List<AndroidProduct> = androidProductRepository.findAll()

    override fun saveAll(androidProducts: List<AndroidProduct>): List<AndroidProduct> =
            androidProductRepository.saveAll(androidProducts)

    override fun save(androidProduct: AndroidProduct): AndroidProduct = androidProductRepository.save(androidProduct)

    override fun deleteById(id: Long) = androidProductRepository.deleteById(id)
}
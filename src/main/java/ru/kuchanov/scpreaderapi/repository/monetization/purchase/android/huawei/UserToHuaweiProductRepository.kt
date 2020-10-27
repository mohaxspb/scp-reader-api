package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.UserToHuaweiProduct

interface UserToHuaweiProductRepository : JpaRepository<UserToHuaweiProduct, Long> {

    @Query("""
        SELECT p from HuaweiProduct p 
            JOIN UserToHuaweiProduct uap ON p.id = uap.huaweiProductId 
            WHERE uap.userId = :userId
    """)

    fun getAndroidProductsByUserId(userId: Long): List<UserToHuaweiProduct>

    fun findAllByUserId(userId: Long): List<UserToHuaweiProduct>
}
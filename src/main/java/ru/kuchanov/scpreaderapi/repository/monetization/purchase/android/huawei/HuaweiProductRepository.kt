package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiProduct

interface HuaweiProductRepository : JpaRepository<HuaweiProduct, Long> {

    @Query("""
        SELECT p from HuaweiProduct p 
            JOIN UserToHuaweiProduct uap ON p.id = uap.huaweiProductId 
            WHERE uap.userId = :userId
    """)
    fun getAndroidProductsByUserId(userId: Long): List<HuaweiProduct>
}
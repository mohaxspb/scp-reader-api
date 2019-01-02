package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct

interface UserAndroidProductRepository : JpaRepository<UsersAndroidProduct, Long> {

    @Query("SELECT p from AndroidProduct p " +
            "JOIN UsersAndroidProduct uap ON p.id = uap.androidProductId " +
            "WHERE uap.userId = :userId")
    fun getAndroidProductsByUserId(userId: Long): List<AndroidProduct>

    fun findAllByUserId(userId: Long): List<UsersAndroidProduct>

    fun getOneByUserIdAndAndroidProductId(userId: Long, androidProductId: Long): UsersAndroidProduct?
}
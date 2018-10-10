package ru.kuchanov.scpreaderapi.repository.purchase

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.AndroidSubscription

interface AndroidSubscriptionRepository : JpaRepository<AndroidSubscription, Long> {
    fun getOneById(id: Long): AndroidSubscription?
}

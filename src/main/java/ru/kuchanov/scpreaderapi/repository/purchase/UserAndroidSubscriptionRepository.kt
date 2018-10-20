package ru.kuchanov.scpreaderapi.repository.purchase

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription

interface UserAndroidSubscriptionRepository : JpaRepository<UsersAndroidSubscription, Long>

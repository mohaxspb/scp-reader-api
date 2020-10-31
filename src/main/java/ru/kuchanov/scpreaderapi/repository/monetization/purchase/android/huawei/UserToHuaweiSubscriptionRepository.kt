package ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.UserToHuaweiSubscription

interface UserToHuaweiSubscriptionRepository : JpaRepository<UserToHuaweiSubscription, Long>
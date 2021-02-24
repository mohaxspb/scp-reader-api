package ru.kuchanov.scpreaderapi.model.dto.monetization

import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscription

data class UserSubscriptionsDto(
        val huaweiSubscriptions: List<HuaweiSubscription>
)
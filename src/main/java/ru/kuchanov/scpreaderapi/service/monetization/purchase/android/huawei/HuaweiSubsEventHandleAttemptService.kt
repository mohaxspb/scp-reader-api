package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionEventHandleAttemptRecord

interface HuaweiSubsEventHandleAttemptService {

    fun save(data: HuaweiSubscriptionEventHandleAttemptRecord): HuaweiSubscriptionEventHandleAttemptRecord

    fun getAll(): List<HuaweiSubscriptionEventHandleAttemptRecord>
}
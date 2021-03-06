package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.huawei

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.huawei.HuaweiSubscriptionEventHandleAttemptRecord
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.huawei.HuaweiSubsEventHandleAttemptRepository

@Service
class HuaweiSubsEventHandleAttemptServiceImpl @Autowired constructor(
        private val repository: HuaweiSubsEventHandleAttemptRepository
) : HuaweiSubsEventHandleAttemptService {

    override fun save(data: HuaweiSubscriptionEventHandleAttemptRecord): HuaweiSubscriptionEventHandleAttemptRecord =
            repository.save(data)

    override fun getAll(): List<HuaweiSubscriptionEventHandleAttemptRecord> =
            repository.findAll()
}
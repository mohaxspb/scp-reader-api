package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscriptionEventHandleAttemptRecord
import ru.kuchanov.scpreaderapi.repository.monetization.purchase.android.google.GoogleSubsEventHandleAttemptRepository

@Service
class GoogleSubsEventHandleAttemptServiceImpl @Autowired constructor(
    private val repository: GoogleSubsEventHandleAttemptRepository
) : GoogleSubsEventHandleAttemptService {

    override fun save(data: GoogleSubscriptionEventHandleAttemptRecord): GoogleSubscriptionEventHandleAttemptRecord =
        repository.save(data)

    override fun getAll(): List<GoogleSubscriptionEventHandleAttemptRecord> =
        repository.findAll()
}
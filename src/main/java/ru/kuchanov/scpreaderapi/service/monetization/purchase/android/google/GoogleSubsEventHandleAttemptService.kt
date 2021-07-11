package ru.kuchanov.scpreaderapi.service.monetization.purchase.android.google

import ru.kuchanov.scpreaderapi.bean.purchase.google.GoogleSubscriptionEventHandleAttemptRecord

interface GoogleSubsEventHandleAttemptService {

    fun save(data: GoogleSubscriptionEventHandleAttemptRecord): GoogleSubscriptionEventHandleAttemptRecord

    fun getAll(): List<GoogleSubscriptionEventHandleAttemptRecord>
}
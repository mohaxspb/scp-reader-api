package ru.kuchanov.scpreaderapi.model.dto.user

import java.time.LocalDateTime

interface UserProjection {
    val id: Long
    val fullName: String?
    val avatar: String?
    var score: Int

    //monetization
    val adsDisabledEndDate: LocalDateTime?
    val offlineLimitDisabledEndDate: LocalDateTime?
    val adsDisabled: Boolean
    val offlineLimitDisabled: Boolean

    //level
    val levelNum: Int
    val scoreToNextLevel: Int
    val curLevelScore: Int

    //articles
    val numOfReadArticles: Int
}
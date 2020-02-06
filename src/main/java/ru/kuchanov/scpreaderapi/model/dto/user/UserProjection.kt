package ru.kuchanov.scpreaderapi.model.dto.user

interface UserProjection {
    val id: Long
    val fullName: String?
    val avatar: String?
    var score: Int
    //level
    val levelNum: Int
    val scoreToNextLevel: Int
    val curLevelScore: Int
    //articles
    val numOfReadArticles: Int
}
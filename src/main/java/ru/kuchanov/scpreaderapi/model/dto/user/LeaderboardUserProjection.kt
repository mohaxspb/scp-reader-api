package ru.kuchanov.scpreaderapi.model.dto.user

interface LeaderboardUserProjection {
    val id: Long
    val avatar: String?
    var fullName: String?
    var score: Int
    //level
    var levelNum: Int
    var scoreToNextLevel: Int
    var curLevelScore: Int
    //articles
    var numOfReadArticles: Int
}
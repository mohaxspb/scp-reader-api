package ru.kuchanov.scpreaderapi.model.user

data class LeaderboardUserDto(
        val id: Long,
        val avatar: String?,
        var fullName: String?,
        var score: Int,
        //level
        var levelNum: Int,
        var scoreToNextLevel: Int,
        var curLevelScore: Int,
        //articles
        var numOfReadArticles: Int
)

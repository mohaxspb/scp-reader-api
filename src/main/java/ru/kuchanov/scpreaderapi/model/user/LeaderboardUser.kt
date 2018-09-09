package ru.kuchanov.scpreaderapi.model.user


data class LeaderboardUser(
        val id: Long,
        val avatar: String,
        var fullName: String,
        var score: Int
)
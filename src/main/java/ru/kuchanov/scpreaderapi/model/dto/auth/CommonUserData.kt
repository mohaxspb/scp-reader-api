package ru.kuchanov.scpreaderapi.model.dto.auth

data class CommonUserData(
        val id: String? = null,
        val email: String? = null,
        val firstName: String? = null,
        val secondName: String? = null,
        val lastName: String? = null,
        val fullName: String? = null,
        val avatarUrl: String? = null
)
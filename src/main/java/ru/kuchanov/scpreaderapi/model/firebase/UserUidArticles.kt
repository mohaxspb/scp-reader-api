package ru.kuchanov.scpreaderapi.model.firebase

import ru.kuchanov.scpreaderapi.bean.users.User

data class UserUidArticles(
        val user: User,
        val uid: String,
        val articles: List<FirebaseArticle>
)
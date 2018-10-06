package ru.kuchanov.scpreaderapi.model.dto.firebase

import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseArticle

data class UserUidArticles(
        val user: User,
        val uid: String,
        val articles: List<FirebaseArticle>
)
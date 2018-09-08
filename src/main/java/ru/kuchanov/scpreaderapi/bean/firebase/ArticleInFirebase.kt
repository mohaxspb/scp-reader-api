package ru.kuchanov.scpreaderapi.bean.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ArticleInFirebase(
        var isFavorite: Boolean = false,
        var isRead: Boolean = false,
        var title: String? = null,
        var url: String? = null,
        var updated: Long = 0
)
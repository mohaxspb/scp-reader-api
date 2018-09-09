package ru.kuchanov.scpreaderapi.model.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseArticle(
        var isFavorite: Boolean = false,
        var isRead: Boolean = false,
        var title: String? = null,
        var url: String? = null,
        var updated: Long = 0
)
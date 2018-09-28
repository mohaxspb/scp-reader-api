package ru.kuchanov.scpreaderapi.model.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class FirebaseArticle(
        @field:JvmField var isFavorite: Boolean? = null,
        @field:JvmField var isRead: Boolean? = null,
        var title: String? = null,
        var url: String? = null,
        var updated: Long? = null
)
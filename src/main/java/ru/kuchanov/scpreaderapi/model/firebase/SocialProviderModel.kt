package ru.kuchanov.scpreaderapi.model.firebase

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class SocialProviderModel(
        var provider: String? = null,
        var id: String? = null
)
package ru.kuchanov.scpreaderapi.model.firebase

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class FirebaseUser(
        var uid: String? = null,
        var fullName: String? = null,
        var avatar: String? = null,
        var email: String? = null,
        var signInRewardGained: Boolean = false,
        var articles: Map<String, @JvmSuppressWildcards FirebaseArticle>? = null,
        var vkGroups: Map<String, @JvmSuppressWildcards Any?>? = null,
        var apps: Map<String, @JvmSuppressWildcards Any?>? = null,
        var score: Int = 0,
        var numOfReadArticles: Int = 0,
        var levelNum: Int = 0,
        var scoreToNextLevel: Int = 0,
        var curLevelScore: Int = 0
) {

    companion object {
        const val FIELD_SCORE = "score"
        const val FIELD_UID = "uid"
    }
}
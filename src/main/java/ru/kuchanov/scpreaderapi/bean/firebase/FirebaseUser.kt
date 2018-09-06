package ru.kuchanov.scpreaderapi.bean.firebase

data class FirebaseUser(
        var uid: String,
        var fullName: String,
        var avatar: String,
        var email: String
) {
    companion object {
        const val FIELD_SCORE = "score"
    }
}
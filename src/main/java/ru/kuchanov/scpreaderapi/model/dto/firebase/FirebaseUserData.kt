package ru.kuchanov.scpreaderapi.model.dto.firebase

import com.google.firebase.auth.UserRecord
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseUser

data class FirebaseUserData(
        val userRecord: UserRecord,
        val firebaseUser: FirebaseUser,
        val lang: ScpReaderConstants.Firebase.FirebaseInstance
)

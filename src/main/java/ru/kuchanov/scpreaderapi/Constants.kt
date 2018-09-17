package ru.kuchanov.scpreaderapi

object Constants {
    /**
     * 12 hours
     */
    const val BACK_UP_RATE_MILLIS = 1000L * 60 * 60 * 12
    /**
     * 24 hours
     */
    const val GALLERY_UPDATE_FROM_VK_RATE_MILLIS = 1000L * 60 * 60 * 24
    /**
     * 12 hours
     */
    const val FIREBASE_USERS_DATA_UPDATE_RATE_MILLIS = 1000L * 60 * 60 * 12
//    const val FIREBASE_USERS_DATA_UPDATE_RATE_MILLIS = 1000L * 30 //30 sec for test

    const val GALLERY_PATH = "gallery"

    const val FIREBASE_PATH = "firebase"

    object LangCodes {
        const val RU = "ru"
        const val EN = "en"
    }

    object Firebase {
        enum class FirebaseInstance(val lang: String) {
            EN("en"),
            RU("ru"),
            PL("pl"),
            DE("de"),
            FR("fr"),
            ES("es"),
            IT("it"),
            PT("pt"),
            CH("ch")
        }
    }
}

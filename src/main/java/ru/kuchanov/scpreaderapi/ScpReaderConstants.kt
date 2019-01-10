package ru.kuchanov.scpreaderapi

object ScpReaderConstants {
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

    const val DEFAULT_AVATAR_URL = "https://pp.userapi.com/c604519/v604519296/46a5/cRsRzeBpbGE.jpg"
    const val DEFAULT_FULL_NAME = "N/A"
    const val DEFAULT_NEW_USER_SCORE = 50

    object Path {
        const val GALLERY = "gallery"
        const val FIREBASE = "firebase"
        const val AUTH = "auth"
        const val PURCHASE = "purchase"
        const val ADS = "ads"
        const val ADS_FILES = "files"
        const val USER = "user"
        const val ARTICLE = "article"
    }

    object FilesPaths {
        const val GALLERY = "gallery"
        const val BANNERS = "banners"
    }

    enum class SocialProvider {
        GOOGLE, FACEBOOK, VK
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

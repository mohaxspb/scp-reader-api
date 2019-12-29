package ru.kuchanov.scpreaderapi

object ScpReaderConstants {

    const val DEFAULT_AVATAR_URL = "https://pp.userapi.com/c604519/v604519296/46a5/cRsRzeBpbGE.jpg"
    const val DEFAULT_FULL_NAME = "N/A"
    const val DEFAULT_NEW_USER_SCORE = 50

    const val NUM_OF_ARTICLES_RATED_PAGE = 20

    object Path {
        const val FIREBASE = "firebase"
        const val AUTH = "auth"
        const val PURCHASE = "purchase"
        const val ADS = "ads"
        const val ADS_FILES = "files"
        const val USER = "user"
        const val ARTICLE = "article"
        const val READ = "read"
        const val FAVORITE = "favorite"
        const val PARSE = "parse"
    }

    object FilesPaths {
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
            ZH("zh")
        }
    }

    enum class ArticleTypeEnum {
        NEUTRAL_OR_NOT_ADDED,
        SAFE,
        EUCLID,
        KETER,
        THAUMIEL,
        NONE
    }
}

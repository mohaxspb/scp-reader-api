package ru.kuchanov.scpreaderapi

object ScpReaderConstants {

    const val DEFAULT_AVATAR_URL = "https://pp.userapi.com/c604519/v604519296/46a5/cRsRzeBpbGE.jpg"
    const val DEFAULT_FULL_NAME = "N/A"
    const val DEFAULT_NEW_USER_SCORE = 50

    const val NUM_OF_ARTICLES_RATED_PAGE = 20

    object InternalAuthData {
        /**
         * used for getting articles and other stuff, that don't need user auth
         */
        const val IMPLICIT_FLOW_CLIENT_ID = "implicit_flow_client_id"
    }

    object Api {
        const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        const val GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials"
        const val PARAM_GRANT_TYPE = "grant_type"
        const val PARAM_REFRESH_TOKEN = "refresh_token"
        const val HEADER_AUTHORIZATION = "Authorization"
        const val HEADER_PART_BEARER = "Bearer"
        const val HEADER_PART_BASIC = "Basic"
    }

    object Score {
        const val DEFAULT_SCORE_FOR_READ_ARTICLE = 1
        const val DEFAULT_SCORE_FOR_REWARDED_VIDEO = 50
    }

    object Path {
        const val FIREBASE = "firebase"
        const val MESSAGING = "messaging"
        const val AUTH = "auth"
        const val MONETIZATION = "monetization"
        const val PURCHASE = "purchase"
        const val ADS = "ads"
        const val ADS_FILES = "files"
        const val USER = "user"
        const val TRANSACTION = "transaction"
        const val ARTICLE = "article"
        const val READ = "read"
        const val FAVORITE = "favorite"
        const val DOWNLOAD = "download"
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
            RU("ru"),
            EN("en"),
            PL("pl"),
            DE("de"),
            FR("fr"),
            ES("es"),
            IT("it"),
            PT("pt"),
            ZH("zh")
        }

        object Fcm {
            enum class Topic(val topicName: String) {
                MAIN("/topics/main")
            }

            enum class MessageType {
                MESSAGE, EXTERNAL_URL, NEW_VERSION
            }

            enum class DataParamName {
                ID, TYPE, TITLE, MESSAGE, URL, UPDATED
            }
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

    enum class UserDataTransactionType {
        READ_ARTICLE, REWARDED_VIDEO
    }

    enum class PushProvider {
        HUAWEI, GOOGLE
    }
}

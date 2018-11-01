package ru.kuchanov.scpreaderapi.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.objects.photos.responses.GetResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import retrofit2.HttpException
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.model.dto.auth.CommonUserData
import ru.kuchanov.scpreaderapi.model.facebook.FacebookProfileResponse
import ru.kuchanov.scpreaderapi.model.facebook.ValidatedTokenWrapper
import javax.annotation.PostConstruct

@Service
class ApiClient {

    companion object {
        const val OK_HTTP_CONNECT_TIMEOUT = 30L
        const val OK_HTTP_READ_TIMEOUT = 30L
        const val OK_HTTP_WRITE_TIMEOUT = 30L
    }

    //facebook
    @Autowired
    private lateinit var facebookApi: FacebookApi

    //google auth
    @Autowired
    private lateinit var googleIdTokenVerifier: GoogleIdTokenVerifier

    //vk
    @Value("\${my.api.vk.art_scp.group_id}")
    var artScpVkGroupId: Int? = null

    @Value("\${my.api.vk.art_scp.album_id}")
    var artScpVkAlbumId: Int? = null

    @Autowired
    private lateinit var vkApiClient: VkApiClient

    @Autowired
    private lateinit var serviceActor: ServiceActor

    //facebook values
    @Value("\${my.api.ru.facebook.client_id}")
    private var facebookClientIdRu: Long? = null
    @Value("\${my.api.ru.facebook.client_secret}")
    private lateinit var facebookClientSecretRu: String

    @Value("\${my.api.en.facebook.client_id}")
    private var facebookClientIdEn: Long? = null
    @Value("\${my.api.en.facebook.client_secret}")
    private lateinit var facebookClientSecretEn: String

    @Value("\${my.api.pl.facebook.client_id}")
    private var facebookClientIdPl: Long? = null
    @Value("\${my.api.pl.facebook.client_secret}")
    private lateinit var facebookClientSecretPl: String

    @Value("\${my.api.de.facebook.client_id}")
    private var facebookClientIdDe: Long? = null
    @Value("\${my.api.de.facebook.client_secret}")
    private lateinit var facebookClientSecretDe: String

    @Value("\${my.api.fr.facebook.client_id}")
    private var facebookClientIdFr: Long? = null
    @Value("\${my.api.fr.facebook.client_secret}")
    private lateinit var facebookClientSecretFr: String

    @Value("\${my.api.es.facebook.client_id}")
    private var facebookClientIdEs: Long? = null
    @Value("\${my.api.es.facebook.client_secret}")
    private lateinit var facebookClientSecretEs: String

    @Value("\${my.api.it.facebook.client_id}")
    private var facebookClientIdIt: Long? = null
    @Value("\${my.api.it.facebook.client_secret}")
    private lateinit var facebookClientSecretIt: String

    @Value("\${my.api.pt.facebook.client_id}")
    private var facebookClientIdPt: Long? = null
    @Value("\${my.api.pt.facebook.client_secret}")
    private lateinit var facebookClientSecretPt: String

    @Value("\${my.api.ch.facebook.client_id}")
    private var facebookClientIdCh: Long? = null
    @Value("\${my.api.ch.facebook.client_secret}")
    private lateinit var facebookClientSecretCh: String

    private lateinit var facebookClientIds: Map<ScpReaderConstants.Firebase.FirebaseInstance, Long>
    private lateinit var facebookClientSecrets: Map<ScpReaderConstants.Firebase.FirebaseInstance, String>

    @PostConstruct
    fun initClassMembers() {
        facebookClientIds = mapOf(
                ScpReaderConstants.Firebase.FirebaseInstance.RU to facebookClientIdRu!!,
                ScpReaderConstants.Firebase.FirebaseInstance.EN to facebookClientIdEn!!,
                ScpReaderConstants.Firebase.FirebaseInstance.PL to facebookClientIdPl!!,
                ScpReaderConstants.Firebase.FirebaseInstance.DE to facebookClientIdDe!!,
                ScpReaderConstants.Firebase.FirebaseInstance.FR to facebookClientIdFr!!,
                ScpReaderConstants.Firebase.FirebaseInstance.ES to facebookClientIdEs!!,
                ScpReaderConstants.Firebase.FirebaseInstance.IT to facebookClientIdIt!!,
                ScpReaderConstants.Firebase.FirebaseInstance.PT to facebookClientIdPt!!,
                ScpReaderConstants.Firebase.FirebaseInstance.CH to facebookClientIdCh!!
        )

        facebookClientSecrets = mapOf(
                ScpReaderConstants.Firebase.FirebaseInstance.RU to facebookClientSecretRu,
                ScpReaderConstants.Firebase.FirebaseInstance.EN to facebookClientSecretEn,
                ScpReaderConstants.Firebase.FirebaseInstance.PL to facebookClientSecretPl,
                ScpReaderConstants.Firebase.FirebaseInstance.DE to facebookClientSecretDe,
                ScpReaderConstants.Firebase.FirebaseInstance.FR to facebookClientSecretFr,
                ScpReaderConstants.Firebase.FirebaseInstance.ES to facebookClientSecretEs,
                ScpReaderConstants.Firebase.FirebaseInstance.IT to facebookClientSecretIt,
                ScpReaderConstants.Firebase.FirebaseInstance.PT to facebookClientSecretPt,
                ScpReaderConstants.Firebase.FirebaseInstance.CH to facebookClientSecretCh
        )
    }

    fun getVkAppAccessToken(): String = vkApiClient.oauth()
            .serviceClientCredentialsFlow(serviceActor.id, serviceActor.clientSecret)
            .execute().accessToken

    fun getScpArtPhotosFromVk(): GetResponse? = vkApiClient.photos().get(serviceActor)
            .ownerId(artScpVkGroupId)
            .albumId(artScpVkAlbumId.toString())
            .photoSizes(true)
            .execute()

    fun getUserDataFromProvider(
            provider: ScpReaderConstants.SocialProvider,
            token: String,
            lang: ScpReaderConstants.Firebase.FirebaseInstance
    ): CommonUserData = when (provider) {
        ScpReaderConstants.SocialProvider.GOOGLE -> {
            val googleIdToken: GoogleIdToken? = googleIdTokenVerifier.verify(token)
            googleIdToken?.let {
                val email = googleIdToken.payload.email ?: throw IllegalStateException("Can't login without email!")
                val avatar = googleIdToken.payload["picture"] as? String
                        ?: ScpReaderConstants.DEFAULT_AVATAR_URL
                val fullName = googleIdToken.payload["name"] as? String
                        ?: ScpReaderConstants.DEFAULT_FULL_NAME
                val firstName = googleIdToken.payload["given_name"] as? String
                        ?: ScpReaderConstants.DEFAULT_FULL_NAME
                val secondName = googleIdToken.payload["family_name"] as? String
                        ?: ScpReaderConstants.DEFAULT_FULL_NAME
                CommonUserData(
                        id = googleIdToken.payload.subject,
                        email = email,
                        firstName = firstName,
                        secondName = secondName,
                        fullName = fullName,
                        avatarUrl = avatar

                )
            } ?: throw IllegalStateException("Failed to verify idToken")
        }
        ScpReaderConstants.SocialProvider.FACEBOOK -> {
            val facebookClientId = facebookClientIds[lang]
            val facebookClientSecret = facebookClientSecrets[lang]
            val validatedTokenWrapper: ValidatedTokenWrapper = facebookApi
                    .debugToken(token, "$facebookClientId|$facebookClientSecret")
                    .map { ValidatedTokenWrapper(it, null) }
                    .onErrorReturn { ValidatedTokenWrapper(null, it) }
                    .blockingGet()

            validatedTokenWrapper.exception?.let {
                throw Exception(
                        if (it is HttpException) {
                            it.response()?.errorBody()?.string()
                        } else {
                            it.message
                        } ?: "unexpected error",
                        it
                )
            }

            if (validatedTokenWrapper.verifiedToken?.data?.appId != facebookClientId) {
                throw IllegalArgumentException("Facebook appId not equals correct one!")
            }

            val facebookProfile: FacebookProfileResponse = facebookApi.profile(token).blockingGet()
            val email = facebookProfile.email ?: throw IllegalStateException("Can't login without email!")
            CommonUserData(
                    id = facebookProfile.id.toString(),
                    email = email,
                    fullName = "${facebookProfile.firstName} ${facebookProfile.lastName}",
                    firstName = facebookProfile.firstName,
                    lastName = facebookProfile.lastName,
                    avatarUrl = facebookProfile.picture?.data?.url
            )
        }
        ScpReaderConstants.SocialProvider.VK -> {
            val commonUserData = ObjectMapper().readValue(token, CommonUserData::class.java)
            if (commonUserData.email == null) {
                throw IllegalStateException("Can't login without email!")
            }

            commonUserData
        }
    }
}
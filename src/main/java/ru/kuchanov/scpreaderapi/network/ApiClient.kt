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
    var facebookClientId: Long? = null
    @Value("\${my.api.ru.facebook.client_secret}")
    lateinit var facebookClientSecret: String

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
            token: String
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
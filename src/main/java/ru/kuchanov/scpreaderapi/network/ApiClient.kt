package ru.kuchanov.scpreaderapi.network

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import retrofit2.HttpException
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.model.dto.auth.CommonUserData
import ru.kuchanov.scpreaderapi.model.facebook.FacebookProfileResponse
import ru.kuchanov.scpreaderapi.model.facebook.ValidatedTokenWrapper
import ru.kuchanov.scpreaderapi.model.huawei.account.HuaweiAccountResponse

@Service
class ApiClient @Autowired constructor(
        private val objectMapper: ObjectMapper,
        private val facebookApi: FacebookApi,
        private val googleIdTokenVerifier: GoogleIdTokenVerifier,
        private val huaweiAuthApi: HuaweiAuthApi,
        private val huaweiAccountApi: HuaweiAccountApi,
        @Value("\${my.api.facebook.client_id}")
        private val facebookClientId: String,
        @Value("\${my.api.facebook.client_secret}")
        private val facebookClientSecret: String,
        @Value("\${my.api.huawei.client_id}")
        private val huaweiClientId: String,
        @Value("\${my.api.huawei.client_secret}")
        private val huaweiClientSecret: String,
        @Value("\${my.api.huawei.redirect_uri}")
        private val huaweiRedirectUri: String,
        private val log: Logger
) {

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
            val validatedTokenWrapper = facebookApi
                    .debugToken(inputToken = token, accessToken = "$facebookClientId|$facebookClientSecret")
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
        ScpReaderConstants.SocialProvider.HUAWEI -> {
            val tokenResponse = huaweiAuthApi.getAccessToken(
                    code = token,
                    redirectUri = huaweiRedirectUri,
                    clientId = huaweiClientId,
                    clientSecret = huaweiClientSecret
            ).execute()

            if (tokenResponse.isSuccessful.not()) {
                throw IllegalStateException("Huawei accessToken request failed. ErrorBody: ${tokenResponse.errorBody()?.string()}")
            }

            val huaweiAccessTokenValue = tokenResponse.body()
                    ?: throw IllegalStateException("Huawei accessToken is null!")

            log.error("tokenResponse body: $huaweiAccessTokenValue")

            val accountResponse = huaweiAccountApi.getAccount(huaweiAccessTokenValue.accessToken).execute()

            if (accountResponse.isSuccessful.not()) {
                throw IllegalStateException("Huawei account request failed. ErrorBody: ${accountResponse.errorBody()?.string()}")
            }

            val accountData: HuaweiAccountResponse = accountResponse.body()
                    ?: throw IllegalStateException("Huawei account is null!")

            CommonUserData(
                    id = accountData.openID,
                    email = accountData.email ?: throw IllegalStateException("Can't login without email!"),
                    fullName = accountData.displayName,
                    firstName = "",
                    lastName = "",
                    avatarUrl = accountData.headPictureURL
            )
        }
        ScpReaderConstants.SocialProvider.VK -> {
            val commonUserData = objectMapper.readValue(token, CommonUserData::class.java)
            if (commonUserData.email == null) {
                throw IllegalStateException("Can't login without email!")
            }

            commonUserData
        }
    }

    companion object {
        const val OK_HTTP_CONNECT_TIMEOUT = 30L
        const val OK_HTTP_READ_TIMEOUT = 30L
        const val OK_HTTP_WRITE_TIMEOUT = 30L
    }
}

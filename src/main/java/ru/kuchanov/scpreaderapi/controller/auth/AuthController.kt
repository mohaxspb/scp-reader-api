package ru.kuchanov.scpreaderapi.controller.auth

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.security.oauth2.core.endpoint.DefaultOAuth2AccessTokenResponseMapConverter
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.InternalAuthData.IMPLICIT_FLOW_CLIENT_ID
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.ClientNotFoundError
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.repository.auth.OauthAuthorizationNewRepository
import ru.kuchanov.scpreaderapi.service.auth.AccessTokenService
import ru.kuchanov.scpreaderapi.service.auth.UserToAuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
import java.io.Serializable
import java.security.Principal
import java.time.Instant
import java.time.ZoneOffset
import java.util.*
import javax.security.auth.message.AuthException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.AUTH)
class AuthController @Autowired constructor(
    private val log: Logger,
    private val clientDetailsService: ClientRegistrationRepository,
    private val userToAuthorityService: UserToAuthorityService,
    private  val usersServiceScpReader: ScpReaderUserService,
    private  val langService: LangService,
    private  val usersLangsService: UsersLangsService,
    private val oauthAuthorizationNewRepository: OauthAuthorizationNewRepository,
    private val oAuth2AuthorizationService: OAuth2AuthorizationService,
    private val registeredClientRepository: RegisteredClientRepository,
    private val apiClient: ApiClient,
    private  val logoutHandler: LogoutHandler
) {

    private val converter = DefaultOAuth2AccessTokenResponseMapConverter()

    @PostMapping("/logout")
    fun logout(
            request: HttpServletRequest,
            response: HttpServletResponse
    ) {
        val auth = SecurityContextHolder.getContext().authentication
        logoutHandler.logout(request, response, auth)
    }

    @PostMapping("/socialLogin")
    fun authorize(
            @RequestParam(value = "provider") provider: ScpReaderConstants.SocialProvider,
            @RequestParam(value = "token") token: String,
            @RequestParam(value = "langId") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "clientId") clientId: String
    ): Map<String, Any> {
        //this will throw error if no client found
        clientDetailsService.findByRegistrationId(clientId)

        val lang = langService.getById(langEnum.lang) ?: throw IllegalArgumentException("Unknown lang: $langEnum")

        val levelsJson = LevelsJson.getLevelsJson()

        val commonUserData = apiClient.getUserDataFromProvider(provider, token, langEnum)

        val email = commonUserData.email ?: throw AuthException("no email found!")

        var userInDb = usersServiceScpReader.getByUsername(email)
        if (userInDb != null) {
            //add provider id to user object if need
            when (provider) {
                ScpReaderConstants.SocialProvider.GOOGLE -> {
                    if (userInDb.googleId.isNullOrEmpty()) {
                        userInDb.googleId = commonUserData.id
                        usersServiceScpReader.update(userInDb)
                    } else if (userInDb.googleId != commonUserData.id) {
                        println("login with ${commonUserData.id}/$email for user with mismatched googleId: ${userInDb.googleId}")
                    }
                }
                ScpReaderConstants.SocialProvider.FACEBOOK -> {
                    if (userInDb.facebookId.isNullOrEmpty()) {
                        userInDb.facebookId = commonUserData.id
                        usersServiceScpReader.update(userInDb)
                    } else if (userInDb.facebookId != commonUserData.id) {
                        println("login with ${commonUserData.id}/$email for user with mismatched facebookId: ${userInDb.facebookId}")
                    }
                }
                ScpReaderConstants.SocialProvider.VK -> {
                    if (userInDb.vkId.isNullOrEmpty()) {
                        userInDb.vkId = commonUserData.id
                        usersServiceScpReader.update(userInDb)
                    } else if (userInDb.vkId != commonUserData.id) {
                        println("login with ${commonUserData.id}/$email for user with mismatched vkId: ${userInDb.vkId}")
                    }
                }
                ScpReaderConstants.SocialProvider.HUAWEI -> {
                    if (userInDb.huaweiId.isNullOrEmpty()) {
                        userInDb.huaweiId = commonUserData.id
                        usersServiceScpReader.update(userInDb)
                    } else if (userInDb.huaweiId != commonUserData.id) {
                        println("login with ${commonUserData.id}/$email for user with mismatched huaweiId: ${userInDb.huaweiId}")
                    }
                }
            }
            return getAccessToken(email, clientId)
        } else {
            //try to find by providers id as email may be changed
            userInDb = usersServiceScpReader.getByProviderId(commonUserData.id!!, provider)
            if (userInDb != null) {
                return getAccessToken(userInDb.username, clientId)
            } else {
                //if cant find - register new user and give it initial score
                val score = ScpReaderConstants.DEFAULT_NEW_USER_SCORE
                val curLevel = levelsJson.getLevelForScore(score)!!

                val userToSave = User(
                    username = email,
                    password = email,
                    avatar = commonUserData.avatarUrl,
                    userAuthorities = setOf(),
                    //firebase
                    fullName = commonUserData.fullName,
                    signInRewardGained = true,
                    score = score,
                    //level
                    levelNum = curLevel.id,
                    curLevelScore = curLevel.score,
                    scoreToNextLevel = levelsJson.scoreToNextLevel(score, curLevel),
                    //misc
                    mainLangId = lang.id
                )
                userToSave.apply {
                    when (provider) {
                        ScpReaderConstants.SocialProvider.GOOGLE -> googleId = commonUserData.id
                        ScpReaderConstants.SocialProvider.FACEBOOK -> facebookId = commonUserData.id
                        ScpReaderConstants.SocialProvider.VK -> vkId = commonUserData.id
                        ScpReaderConstants.SocialProvider.HUAWEI -> huaweiId = commonUserData.id
                    }
                }

                val newUserInDb = usersServiceScpReader.create(userToSave)

                userToAuthorityService.save(UserToAuthority(userId = newUserInDb.id!!, authority = AuthorityType.USER))
                usersLangsService.insert(UsersLangs(userId = newUserInDb.id, langId = lang.id))

                return getAccessToken(newUserInDb.username, clientId)
            }
        }
    }

    fun getAccessToken(email: String, clientId: String): Map<String, Any> {
        println("getAccessToken: $email")
        var tokenNew = oauthAuthorizationNewRepository.findFirstByPrincipalName(email)
        println("tokenNew: ${tokenNew?.accessTokenValue}")
        val registeredClient = registeredClientRepository.findByClientId(clientId) ?: throw ClientNotFoundError()
        val scopes = registeredClient.scopes
        if (tokenNew == null) {
            println("getAccessToken generate new one")

            val keyGen = Base64StringKeyGenerator()
            val accessTokenValue = keyGen.generateKey()
            val refreshTokenValue = keyGen.generateKey()

            val accessTokenObject = OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue,
                Instant.now(),
                Instant.now().plusMillis(registeredClient.tokenSettings.accessTokenTimeToLive.toMillis()),
                scopes
            )
            val now = Instant.now()

            val refreshTokenObject = OAuth2RefreshToken(
                refreshTokenValue,
                now,
                now.plusMillis(registeredClient.tokenSettings.refreshTokenTimeToLive.toMillis())
            )

            val user = usersServiceScpReader.loadUserByUsername(email)

            val authorization = OAuth2Authorization
                .withRegisteredClient(registeredClient)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .principalName(user.username)
                .accessToken(accessTokenObject)
                .refreshToken(refreshTokenObject)
                .attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, scopes)
                .attribute(
                    Principal::class.java.name,
                    UsernamePasswordAuthenticationToken(
                        user.username,
                        user.password,
                        user.authorities
                    )
                )
                .build()


            oAuth2AuthorizationService.save(authorization)

            tokenNew = oauthAuthorizationNewRepository.findFirstByPrincipalName(email)!!
        }

        val tokenResponse =  OAuth2AccessTokenResponse
            .withToken(tokenNew.accessTokenValue)
            .tokenType(OAuth2AccessToken.TokenType.BEARER)
            .refreshToken(tokenNew.refreshTokenValue)
            .scopes(scopes)
            .expiresIn(
                tokenNew.accessTokenExpiresAt!!.minusSeconds(
                    tokenNew.accessTokenIssuedAt!!.toEpochSecond(ZoneOffset.UTC)
                ).toEpochSecond(ZoneOffset.UTC)
            )
            .build()

        return converter.convert(tokenResponse)!!
    }

//    fun generateAccessToken(email: String, clientId: String): OAuth2AccessToken {
//        val clientDetails = clientDetailsService.loadClientByClientId(clientId)
//
//        val requestParameters = mapOf<String, String>()
//        val authorities = clientDetails.authorities
//        val approved = true
//        val scope = clientDetails.scope
//        val resourceIds = clientDetails.resourceIds
//        val redirectUri = null
//        val responseTypes = setOf("code")
//        val extensionProperties = HashMap<String, Serializable>()
//
//        val oAuth2Request = OAuth2Request(
//                requestParameters,
//                clientId,
//                authorities,
//                approved,
//                scope,
//                resourceIds,
//                redirectUri,
//                responseTypes,
//                extensionProperties
//        )
//
//        val user: User = usersServiceScpReader.loadUserByUsername(email) ?: throw UserNotFoundException()
//        val authenticationToken = UsernamePasswordAuthenticationToken(
//                user,
//                user.password,
//                authorities
//        )
//
//        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)
//
//        return tokenServices.createAccessToken(auth)
//    }
}

package ru.kuchanov.scpreaderapi.controller.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.service.auth.AccessTokenServiceImpl
import ru.kuchanov.scpreaderapi.service.auth.ScpOAuth2AuthorizationService
import ru.kuchanov.scpreaderapi.service.auth.UserToAuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.security.auth.message.AuthException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.AUTH)
class AuthController @Autowired constructor(
    private val clientDetailsService: ClientRegistrationRepository,
    private val userToAuthorityService: UserToAuthorityService,
    private val usersServiceScpReader: ScpReaderUserService,
    private val langService: LangService,
    private val usersLangsService: UsersLangsService,
    private val oAuth2AuthorizationService: ScpOAuth2AuthorizationService,
    private val apiClient: ApiClient,
    private val logoutHandler: LogoutHandler,
    private val registeredClientRepository: RegisteredClientRepository,
    /**
     * Used as we need to search for old tokens
     */
    private val accessTokenService: AccessTokenServiceImpl,
) {

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val auth = SecurityContextHolder.getContext().authentication
        logoutHandler.logout(request, response, auth)
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/getAccessTokenForEmail")
    fun getAccessTokenForEmail(
        @AuthenticationPrincipal user: User,
        @RequestParam(value = "email") email: String,
        @RequestParam(value = "oldAuth") oldAuth: Boolean,
    ): Map<String, Any> {
        println("getAccessTokenForEmail: $user")
        val userInDb = usersServiceScpReader.getByUsername(email)!!
        if (oldAuth.not()) {
            return oAuth2AuthorizationService.generateAndSaveToken(userInDb.username, "client_id")
        } else {
            return accessTokenService.findFirstByUserName(userInDb.username)!!.let {
                val client = registeredClientRepository.findByClientId(it.clientId)!!
                val accessTokenValueDeserialized = accessTokenService
                    .deserialize<DefaultOAuth2AccessToken>(it.token)
                val parameters: MutableMap<String, Any> = mutableMapOf()
                parameters["access_token"] = accessTokenValueDeserialized.value
                parameters["token_type"] = "bearer"
                parameters["expires_in"] = ChronoUnit.SECONDS.between(
                    accessTokenValueDeserialized.expiration.toInstant()
                        .minusMillis(client.tokenSettings.accessTokenTimeToLive.toMillis()),
                    Instant.now()
                )
                parameters["scope"] = client.scopes.joinToString(separator = ",")

                parameters["refresh_token"] = accessTokenValueDeserialized.refreshToken.value

                parameters
            }
        }
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
            return oAuth2AuthorizationService.generateAndSaveToken(email, clientId)
        } else {
            //try to find by providers id as email may be changed
            userInDb = usersServiceScpReader.getByProviderId(commonUserData.id!!, provider)
            if (userInDb != null) {
                return oAuth2AuthorizationService.generateAndSaveToken(userInDb.username, clientId)
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

                return oAuth2AuthorizationService.generateAndSaveToken(newUserInDb.username, clientId)
            }
        }
    }
}

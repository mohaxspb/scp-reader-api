package ru.kuchanov.scpreaderapi.controller.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.service.auth.ScpOAuth2AuthorizationService
import ru.kuchanov.scpreaderapi.service.auth.UserToAuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
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
    private val logoutHandler: LogoutHandler
) {

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

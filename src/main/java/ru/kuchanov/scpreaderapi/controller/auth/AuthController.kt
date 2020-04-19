package ru.kuchanov.scpreaderapi.controller.auth

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.service.auth.UserToAuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
import java.io.Serializable
import java.util.*
import javax.security.auth.message.AuthException


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.AUTH)
class AuthController @Autowired constructor(
        val log: Logger,
        val clientDetailsService: ClientDetailsService,
        val userToAuthorityService: UserToAuthorityService,
        val usersServiceScpReader: ScpReaderUserService,
        val langService: LangService,
        val usersLangsService: UsersLangsService,
        val tokenStore: TokenStore,
        val tokenServices: DefaultTokenServices,
        val apiClient: ApiClient
) {

    @PostMapping("/socialLogin")
    fun authorize(
            @RequestParam(value = "provider") provider: ScpReaderConstants.SocialProvider,
            @RequestParam(value = "token") token: String,
            @RequestParam(value = "langId") langEnum: ScpReaderConstants.Firebase.FirebaseInstance,
            @RequestParam(value = "clientId") clientId: String
    ): OAuth2AccessToken? {
        //this will throw error if no client found
        clientDetailsService.loadClientByClientId(clientId)

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
                        usersServiceScpReader.save(userInDb)
                    } else if (userInDb.googleId != commonUserData.id) {
                        log.error("login with ${commonUserData.id}/$email for user with mismatched googleId: ${userInDb.googleId}")
                    }
                }
                ScpReaderConstants.SocialProvider.FACEBOOK -> {
                    if (userInDb.facebookId.isNullOrEmpty()) {
                        userInDb.facebookId = commonUserData.id
                        usersServiceScpReader.save(userInDb)
                    } else if (userInDb.facebookId != commonUserData.id) {
                        log.error("login with ${commonUserData.id}/$email for user with mismatched facebookId: ${userInDb.facebookId}")
                    }
                }
                ScpReaderConstants.SocialProvider.VK -> {
                    if (userInDb.vkId.isNullOrEmpty()) {
                        userInDb.vkId = commonUserData.id
                        usersServiceScpReader.save(userInDb)
                    } else if (userInDb.vkId != commonUserData.id) {
                        log.error("login with ${commonUserData.id}/$email for user with mismatched vkId: ${userInDb.vkId}")
                    }
                }
            }
            revokeUserTokens(email, clientId)
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
                    }
                }

                val newUserInDb = usersServiceScpReader.save(userToSave)

                userToAuthorityService.save(UserToAuthority(userId = newUserInDb.id!!, authority = AuthorityType.USER))
                usersLangsService.insert(UsersLangs(userId = newUserInDb.id, langId = lang.id))

                return getAccessToken(newUserInDb.username, clientId)
            }
        }
    }

    private fun revokeUserTokens(email: String, clientId: String) =
            tokenStore
                    .findTokensByClientIdAndUserName(clientId, email)
                    .forEach { tokenServices.revokeToken(it.value) }

    fun getAccessToken(email: String, clientId: String): OAuth2AccessToken {
        val clientDetails = clientDetailsService.loadClientByClientId(clientId)

        val requestParameters = mapOf<String, String>()
        val authorities = clientDetails.authorities
        val approved = true
        val scope = clientDetails.scope
        val resourceIds = clientDetails.resourceIds
        val redirectUri = null
        val responseTypes = setOf("code")
        val extensionProperties = HashMap<String, Serializable>()

        val oAuth2Request = OAuth2Request(
                requestParameters,
                clientId,
                authorities,
                approved,
                scope,
                resourceIds,
                redirectUri,
                responseTypes,
                extensionProperties
        )

        val user: User = usersServiceScpReader.loadUserByUsername(email) ?: throw UserNotFoundException()
        val authenticationToken = UsernamePasswordAuthenticationToken(
                user,
                user.password,
                authorities
        )

        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)

        return tokenServices.createAccessToken(auth)
    }
}

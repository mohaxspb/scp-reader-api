package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.Authority
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.UserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
import java.io.Serializable
import java.util.*
import javax.security.auth.message.AuthException


@RestController
@RequestMapping("/${ScpReaderConstants.Path.AUTH}")
class AuthController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var authorityService: AuthorityService

    @Autowired
    private lateinit var firebaseService: FirebaseService

    @Autowired
    private lateinit var usersService: UserService

    @Autowired
    private lateinit var langService: LangService

    @Autowired
    private lateinit var usersLangsService: UsersLangsService

    @Autowired
    private lateinit var tokenServices: DefaultTokenServices

    @Autowired
    private lateinit var apiClient: ApiClient

    @PostMapping("/socialLogin")
    fun authorize(
            @RequestParam(value = "provider") provider: ScpReaderConstants.SocialProvider,
            @RequestParam(value = "token") token: String,
            @RequestParam(value = "langId") langEnum: ScpReaderConstants.Firebase.FirebaseInstance
    ): OAuth2AccessToken? {
        println("authorize called")

        val lang = langService.getById(langEnum.lang) ?: throw IllegalArgumentException("Unknown lang: $langEnum")

        val levelsJson = LevelsJson.getLevelsJson()

        val commonUserData = apiClient.getUserDataFromProvider(provider, token)

        val email = commonUserData.email ?: throw AuthException("no email found!")

        var userInDb = usersService.getByUsername(email)
        if (userInDb != null) {
            //add provider id to user object if need
            when (provider) {
                ScpReaderConstants.SocialProvider.GOOGLE -> {
                    if (userInDb.googleId.isNullOrEmpty()) {
                        userInDb.googleId = commonUserData.id
                        usersService.update(userInDb)
                    } else if (userInDb.googleId != commonUserData.id) {
                        log.error("login with ${commonUserData.id}/$email for user with mismatched googleId: ${userInDb.googleId}")
                    }
                }
                ScpReaderConstants.SocialProvider.FACEBOOK -> {
                    if (userInDb.facebookId.isNullOrEmpty()) {
                        userInDb.facebookId = commonUserData.id
                        usersService.update(userInDb)
                    } else if (userInDb.facebookId != commonUserData.id) {
                        log.error("login with ${commonUserData.id}/$email for user with mismatched facebookId: ${userInDb.facebookId}")
                    }
                }
                ScpReaderConstants.SocialProvider.VK -> {
                    if (userInDb.vkId.isNullOrEmpty()) {
                        userInDb.vkId = commonUserData.id
                        usersService.update(userInDb)
                    } else if (userInDb.vkId != commonUserData.id) {
                        log.error("login with ${commonUserData.id}/$email for user with mismatched vkId: ${userInDb.vkId}")
                    }
                }
            }
            return getAccessToken(email)
        } else {
            //try to find by providers id as email may be changed
            userInDb = usersService.getByProviderId(commonUserData.id!!, provider)
            if (userInDb != null) {
                return getAccessToken(userInDb.username)
            } else {
                //search in firebase auth api for all apps
                //and collect user data from all apps (score and read/favorite articles)
                val userDataFromFirebase = firebaseService.getUsersDataFromFirebaseByEmail(email)
                if (userDataFromFirebase.isNotEmpty()) {
                    //register user with max score and providerId
                    val maxScore = userDataFromFirebase.maxBy { it.firebaseUser.score }!!.firebaseUser.score
                    val curLevel = levelsJson.getLevelForScore(maxScore)!!
                    val firebaseUser = userDataFromFirebase.first().firebaseUser
                    userInDb = usersService.insert(User(
                            myUsername = email,
                            myPassword = email,
                            avatar = firebaseUser.avatar,
                            userAuthorities = setOf(),
                            //firebase
                            fullName = firebaseUser.fullName,
                            signInRewardGained = firebaseUser.signInRewardGained,
                            score = maxScore,
                            //level
                            levelNum = curLevel.id,
                            curLevelScore = curLevel.score,
                            scoreToNextLevel = levelsJson.scoreToNextLevel(maxScore, curLevel),
                            //misc
                            mainLangId = lang.id
                    ).apply {
                        when (provider) {
                            ScpReaderConstants.SocialProvider.GOOGLE -> googleId = commonUserData.id
                            ScpReaderConstants.SocialProvider.FACEBOOK -> facebookId = commonUserData.id
                            ScpReaderConstants.SocialProvider.VK -> vkId = commonUserData.id
                        }
                    })

                    authorityService.insert(Authority(userInDb.id, AuthorityType.USER.name))

                    //and save his read/favorite articles

                    userDataFromFirebase.forEach { firebaseUserData ->
                        //add user-lang connection if need
                        if (usersLangsService.getByUserIdAndLangId(userInDb.id!!, lang.id) == null) {
                            usersLangsService.insert(UsersLangs(userInDb.id!!, lang.id))
                        }

                        firebaseService.manageFirebaseArticlesForUser(
                                firebaseUserData.firebaseUser.articles?.values?.toList()
                                        ?: listOf(),
                                userInDb,
                                langService.getById(firebaseUserData.lang.lang)
                                        ?: throw IllegalArgumentException("Unknown lang: ${firebaseUserData.lang}")
                        )
                    }

                    return getAccessToken(userInDb.username)
                } else {
                    //if cant find - register new user and give it initial score
                    val score = ScpReaderConstants.DEFAULT_NEW_USER_SCORE
                    val curLevel = levelsJson.getLevelForScore(score)!!

                    val newUserInDb = usersService.insert(User(
                            myUsername = email,
                            myPassword = email,
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
                    ).apply {
                        when (provider) {
                            ScpReaderConstants.SocialProvider.GOOGLE -> googleId = commonUserData.id
                            ScpReaderConstants.SocialProvider.FACEBOOK -> facebookId = commonUserData.id
                            ScpReaderConstants.SocialProvider.VK -> vkId = commonUserData.id
                        }
                    })

                    authorityService.insert(Authority(newUserInDb.id, AuthorityType.USER.name))
                    usersLangsService.insert(UsersLangs(newUserInDb.id!!, lang.id))

                    return getAccessToken(newUserInDb.username)
                }
            }
        }
    }

    fun getAccessToken(email: String): OAuth2AccessToken? {
        val requestParameters = mapOf<String, String>()
        //todo move to constants
        val clientId = "client_id"
        //todo move to constants
        val authorities = setOf(SimpleGrantedAuthority("USER"))
        val approved = true
        //todo move to constants
        val scope = setOf("read write")
        val resourceIds = setOf<String>()
        val responseTypes = setOf("code")
        val extensionProperties = HashMap<String, Serializable>()

        val oAuth2Request = OAuth2Request(
                requestParameters,
                clientId,
                authorities,
                approved,
                scope,
                resourceIds,
                null,
                responseTypes,
                extensionProperties
        )

        val authenticationToken = UsernamePasswordAuthenticationToken(
                usersService.loadUserByUsername(email),
                null,
                authorities
        )
        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)
        return tokenServices.createAccessToken(auth)
    }
}
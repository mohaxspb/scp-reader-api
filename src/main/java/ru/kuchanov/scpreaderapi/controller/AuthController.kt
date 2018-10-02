package ru.kuchanov.scpreaderapi.controller

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
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
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.firebase.FirebaseService
import ru.kuchanov.scpreaderapi.service.users.UserService
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
    private lateinit var tokenServices: DefaultTokenServices

    @Autowired
    private lateinit var googleIdTokenVerifier: GoogleIdTokenVerifier

    @PostMapping("/socialLogin")
    fun authorize(
            @RequestParam(value = "provider") provider: ScpReaderConstants.SocialProvider,
            @RequestParam(value = "token") token: String,
            @RequestParam(value = "langId") lang: ScpReaderConstants.Firebase.FirebaseInstance
    ): OAuth2AccessToken? {
        println("authorize called")

        val levelsJson = LevelsJson.getLevelsJson()

        when (provider) {
            ScpReaderConstants.SocialProvider.GOOGLE -> {
                val googleIdToken: GoogleIdToken? = googleIdTokenVerifier.verify(token)
                println(googleIdToken)
                if (googleIdToken == null) {
                    throw IllegalStateException("Failed to verify idToken")
                }
                val email = googleIdToken.payload.email ?: throw AuthException("no email found!")
                var userInDb = usersService.getByUsername(email)
                if (userInDb != null) {
                    //add google id to user object if need
                    if (userInDb.googleId.isNullOrEmpty()) {
                        userInDb.googleId = googleIdToken.payload.subject
                        usersService.update(userInDb)
                    } else if (userInDb.googleId != googleIdToken.payload.subject) {
                        log.error("login with ${googleIdToken.payload.subject}/${googleIdToken.payload.email} " +
                                "for user with mismatched googleId: ${userInDb.googleId}")
                    }
                    return getAccessToken(googleIdToken.payload.email)
                } else {
                    //try to find by providers id as email may be changed
                    userInDb = usersService.getByProviderId(googleIdToken.payload.subject, provider)
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
                                    googleId = googleIdToken.payload.subject
                            ))
                            authorityService.insert(Authority(userInDb.id, AuthorityType.USER.name))

                            //todo and save his read/favorite articles (check if we can use firebase service from 237 line)
                        } else {
                            //todo if cant find - register new user and give it initial score
                        }
                    }
                }
            }
            //todo add facebook and VK
            //todo add VK
            else -> throw IllegalArgumentException("Unexpected provider: $provider")
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
        println("${tokenServices.getAccessToken(auth)}")
        return tokenServices.createAccessToken(auth)
    }
}
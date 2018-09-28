package ru.kuchanov.scpreaderapi.service.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.bean.articles.*
import ru.kuchanov.scpreaderapi.bean.auth.Authority
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseArticle
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseUser
import ru.kuchanov.scpreaderapi.model.firebase.UserUidArticles
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.repository.firebase.FirebaseDataUpdateDateRepository
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ArticleService
import ru.kuchanov.scpreaderapi.service.article.FavoriteArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ReadArticleForLangService
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.UserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
import java.sql.Timestamp
import java.time.Instant
import javax.transaction.Transactional

@Service
class FirebaseService {

    companion object {
        const val QUERY_LIMIT = 100
    }

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var authorityService: AuthorityService

    @Autowired
    private lateinit var langService: LangService

    @Autowired
    private lateinit var usersLangsService: UsersLangsService

    @Autowired
    private lateinit var articleService: ArticleService

    @Autowired
    private lateinit var articleForLangService: ArticleForLangService

    @Autowired
    private lateinit var favoriteArticleForLangService: FavoriteArticleForLangService

    @Autowired
    private lateinit var readArticleForLangService: ReadArticleForLangService

    @Autowired
    private lateinit var firebaseDataUpdateDateRepository: FirebaseDataUpdateDateRepository

    fun getAllUsersForLang(langId: String) = userService.getAllUsersByLangId(langId)

    fun getUsersByLangIdCount(langId: String) = userService.getUsersByLangIdCount(langId)

    @Async
    fun updateDataFromFirebase(startKey: String = "", langToParse: Constants.Firebase.FirebaseInstance? = null) {
        println("updateDataFromFirebase")
        //todo use rx for whole method
        Constants.Firebase.FirebaseInstance.values()
                .filter { if (langToParse == null) true else it == langToParse }
                .forEach { lang ->
                    println("query for lang: $lang")
                    val firebaseDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance(lang.lang))

                    val subject = BehaviorProcessor.createDefault(startKey)

                    subject
                            .concatMap { startKey -> usersObservable(firebaseDatabase, startKey).toFlowable() }
                            .map {
                                insertUsers(it, langService.getById(lang.lang))
                                it
                            }
                            .doOnNext { users ->
                                if (users.size != QUERY_LIMIT) {
                                    subject.onComplete()
                                } else {
                                    subject.onNext(users.last().uid)
                                }
                            }
                            .doOnNext { println("users size: ${it.size}") }
                            .toList()
                            .map { it.flatten() }
                            .subscribeBy(
                                    onSuccess = {
                                        updateFirebaseUpdateDate(lang.lang)

                                        println("done updating users for lang: ${lang.lang}, totalCount: ${it.size}")
                                    },
                                    onError = {
                                        println("error in update users observable: $it")
                                        log.error("error in update users observable: ", it)
                                    }
                            )
                }
    }

    private fun updateFirebaseUpdateDate(langId: String) {
        var firebaseUpdateDate = firebaseDataUpdateDateRepository.findOneByLangId(langId)

        if (firebaseUpdateDate == null) {
            firebaseUpdateDate = FirebaseDataUpdateDate(langId = langId, updated = Timestamp.from(Instant.now()))
            firebaseDataUpdateDateRepository.save(firebaseUpdateDate)
        } else {
            firebaseUpdateDate.updated = Timestamp.from(Instant.now())
            firebaseDataUpdateDateRepository.save(firebaseUpdateDate)
        }
    }

    fun getAllFirebaseUpdatedDataDates(): MutableList<FirebaseDataUpdateDate> = firebaseDataUpdateDateRepository.findAll()

    @Transactional
    private fun insertUsers(firebaseUsers: List<FirebaseUser>, lang: Lang) {
        println("insertUsers: ${lang.id}/${firebaseUsers.size}")

        var newUsersInserted = 0
        var newLangForExistedUsers = 0

        val levelsJson = LevelsJson.getLevelsJson()

//        println("levelsJson: $levelsJson")

        firebaseUsers
                .distinctBy { it.email }
                .filter { it.email != null }
                .map {
                    //set level info
//                    println("it.score: ${it.score}/${it.uid}")
                    //there are users with negative score, so just set it to 0
                    if (it.score < 0) {
                        it.score = 0
                    }
                    val curLevel = levelsJson.getLevelForScore(it.score)!!
                    UserUidArticles(
                            User(
                                    myUsername = it.email!!,
                                    myPassword = it.email!!,
                                    avatar = it.avatar,
                                    userAuthorities = setOf(),
                                    //firebase
                                    fullName = it.fullName,
                                    signInRewardGained = it.signInRewardGained,
                                    score = it.score,
                                    //level
                                    levelNum = curLevel.id,
                                    curLevelScore = curLevel.score,
                                    scoreToNextLevel = levelsJson.scoreToNextLevel(it.score, curLevel)
                            ),
                            it.uid!!,
                            it.articles?.values?.toList() ?: listOf()
                    )
                }
                .forEach { userUidArticles ->
                    //check if user already exists and update just some values
                    var userInDb = userService.getByUsername(userUidArticles.user.myUsername)
                    if (userInDb == null) {
                        if (userUidArticles.user.avatar?.startsWith("data:image") == true) {
                            userUidArticles.user.avatar = Constants.DEFAULT_AVATAR_URL
                            println("insert user with base64 avatar: ${userUidArticles.user}")
                        }
                        userInDb = userService.insert(userUidArticles.user)
                        authorityService.insert(Authority(userInDb.id, AuthorityType.USER.name))
                        newUsersInserted++
                    }

                    //add user-lang connection if need
                    if (usersLangsService.getByUserIdAndLangId(userInDb.id!!, lang.id) == null) {
                        usersLangsService.insert(UsersLangs(userInDb.id!!, lang.id, userUidArticles.uid))
                        newLangForExistedUsers++
                    }

                    //increase score if need
                    val firebaseUser = userUidArticles.user
                    if (userInDb.score!! <= firebaseUser.score!!) {
                        userInDb.score = firebaseUser.score
                        //set level info
                        val curLevel = levelsJson.getLevelForScore(userInDb.score!!)!!
                        userInDb.levelNum = curLevel.id
                        userInDb.curLevelScore = curLevel.score
                        userInDb.scoreToNextLevel = levelsJson.scoreToNextLevel(userInDb.score!!, curLevel)
                        //update user in DB
                        userService.update(userInDb)
                    }

                    //insert read/favorite articles if need
                    manageFirebaseArticlesForUser(userUidArticles.articles, userInDb, lang)
                }

        println("newUsersInserted = $newUsersInserted, newLangForExistedUsers = $newLangForExistedUsers")
    }

    private fun manageFirebaseArticlesForUser(
            articlesInFirebase: List<FirebaseArticle>,
            user: User,
            lang: Lang
    ) {
//        println("manageFirebaseArticlesForUser: ${lang.id}/${user.username}")
        articlesInFirebase.forEachIndexed { index, articleInFirebase ->
            //            if (index == 1) return@forEachIndexed
            if (articleInFirebase.url == null) {
                println("manageFirebaseArticlesForUser: ${lang.id}/${user.username}")
                println("articleInFirebase: $index/$articleInFirebase")
                return@forEachIndexed
            }
            val urlRelative = articleInFirebase.url!!.replace("${lang.siteBaseUrl}/", "")
            //for other langs we should not pass urlRelative
            var articleInDb = articleService.getArticleByUrlRelative(urlRelative)
//            println("articleInDb: $articleInDb $urlRelative")
            //insert new article and article-lang connection if need
            if (articleInDb == null) {
                articleInDb = articleService.insert(Article())
            }
            //check if we do not have article-lang connection for given article
            if (articleForLangService.getArticleForLang(articleInDb.id!!, lang.id) == null) {
                articleForLangService.insert(ArticleForLang(
                        articleId = articleInDb.id!!,
                        langId = lang.id,
                        urlRelative = urlRelative,
                        title = articleInFirebase.title
                ))
            }
            //update favorite if need
            manageFavoriteArticlesForUserForLang(user, articleInFirebase, articleInDb, lang)

            //update read if need
            manageReadArticlesForUserForLang(user, articleInFirebase, articleInDb, lang)
        }
    }

    private fun manageFavoriteArticlesForUserForLang(
            user: User,
            articleInFirebase: FirebaseArticle,
            articleInDb: Article,
            lang: Lang
    ) {
        var favoriteArticleForLang = favoriteArticleForLangService.getFavoriteArticleForArticleIdLangIdAndUserId(
                userId = user.id!!,
                articleId = articleInDb.id!!,
                langId = lang.id
        )

        if (favoriteArticleForLang == null) {
            favoriteArticleForLang = favoriteArticleForLangService.insert(FavoriteArticlesByLang(
                    userId = user.id,
                    articleId = articleInDb.id,
                    langId = lang.id,
                    isFavorite = articleInFirebase.isFavorite!!
            ))
        }

        val dateInFirebase = articleInFirebase.updated
        val dateInDb = favoriteArticleForLang.updated!!.time

        //check timestamp and update if need
        if (dateInDb < dateInFirebase!!) {
            //outdated info in DB, so update it if value changed
            if (favoriteArticleForLang.isFavorite != articleInFirebase.isFavorite) {
                favoriteArticleForLang.isFavorite = articleInFirebase.isFavorite!!
                favoriteArticleForLangService.update(favoriteArticleForLang)
            }
        }
    }

    private fun manageReadArticlesForUserForLang(
            user: User,
            articleInFirebase: FirebaseArticle,
            articleInDb: Article,
            lang: Lang
    ) {
        var readArticleForLang = readArticleForLangService.getReadArticleForArticleIdLangIdAndUserId(
                userId = user.id!!,
                articleId = articleInDb.id!!,
                langId = lang.id
        )

        if (readArticleForLang == null) {
            readArticleForLang = readArticleForLangService.insert(ReadArticlesByLang(
                    userId = user.id,
                    articleId = articleInDb.id,
                    langId = lang.id,
                    isRead = articleInFirebase.isRead!!
            ))
        }

        val dateInFirebase = articleInFirebase.updated
        val dateInDb = readArticleForLang.updated!!.time

        //check timestamp and update if need
        if (dateInDb < dateInFirebase!!) {
            //outdated info in DB, so update it if value changed
            if (readArticleForLang.isRead != articleInFirebase.isRead) {
                readArticleForLang.isRead = articleInFirebase.isRead!!
                readArticleForLangService.update(readArticleForLang)
            }
        }
    }

    private fun usersObservable(
            firebaseDatabase: FirebaseDatabase,
            startAtKey: String? = null
    ): Single<List<FirebaseUser>> {
//        println("usersObservable: $startAtKey")
        return Single.create { subscriber ->
            var query = firebaseDatabase
                    .getReference("users")
                    .orderByKey()
                    .limitToFirst(QUERY_LIMIT)
            if (startAtKey != null) {
                query = query.startAt(startAtKey)
            }
//            println("query: ${query.spec}")

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {
                    println("error?.message: ${error?.message}")
                    subscriber.onError(error?.toException()!!)
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val firebaseUsers = snapshot?.children?.filter { it.hasChildren() }?.map {
                        try {
                            it.getValue(FirebaseUser::class.java)
                        } catch (e: Exception) {
                            println("error while parse user: $e")
                            println("KEY IS: ${it.key}")
                            log.error("error while parse user: ", e)
                            log.error("KEY IS: ${it.key}")
                            throw RuntimeException("error convert data")
                        }
                    }
                    println("firebaseUsers: ${firebaseUsers?.map { it.email }}")
                    subscriber.onSuccess(firebaseUsers!!)
                }
            })
        }
    }
}
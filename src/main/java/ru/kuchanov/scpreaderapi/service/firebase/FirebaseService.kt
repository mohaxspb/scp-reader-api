package ru.kuchanov.scpreaderapi.service.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.rxkotlin.subscribeBy
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.FirebaseDataUpdateDate
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.favorite.FavoriteArticleByLang
import ru.kuchanov.scpreaderapi.bean.auth.AuthorityType
import ru.kuchanov.scpreaderapi.bean.auth.UserToAuthority
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.dto.firebase.UserUidArticles
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseArticle
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseUser
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.repository.firebase.FirebaseDataUpdateDateRepository
import ru.kuchanov.scpreaderapi.repository.transaction.UserDataTransactionService
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ArticleService
import ru.kuchanov.scpreaderapi.service.article.favorite.FavoriteArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.read.ReadArticleForLangService
import ru.kuchanov.scpreaderapi.service.auth.UserToAuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.UserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
import java.sql.Timestamp
import java.time.Instant
import javax.transaction.Transactional

@Service
class FirebaseService @Autowired constructor(
        val log: Logger,
        val userService: UserService,
        val userToAuthorityService: UserToAuthorityService,
        val langService: LangService,
        val usersLangsService: UsersLangsService,
        val articleService: ArticleService,
        val articleForLangService: ArticleForLangService,
        val favoriteArticleForLangService: FavoriteArticleForLangService,
        val readArticleForLangService: ReadArticleForLangService,
        val transactionService: UserDataTransactionService,
        val firebaseDataUpdateDateRepository: FirebaseDataUpdateDateRepository
) {

    @Async
    fun updateDataFromFirebase(
            startKey: String = "",
            langToParse: ScpReaderConstants.Firebase.FirebaseInstance? = null,
            maxUsersCount: Int? = null
    ) {
        println("updateDataFromFirebase")
        log.error("updateDataFromFirebase start")

        Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                .filter { if (langToParse == null) true else it == langToParse }
                .flatMapSingle { lang ->
                    println("Start parsing firebase for lang: ${lang.lang}")
                    val langInDb = lang.let { langService.getById(it.lang) }
                            ?: throw IllegalArgumentException("Unknown lang: $lang")

                    val firebaseDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance(lang.lang))

                    val subject = BehaviorProcessor.createDefault(startKey)

                    subject
                            .concatMap { startKey -> usersObservable(firebaseDatabase, startKey).toFlowable() }
                            .map {
                                if (maxUsersCount != null && maxUsersCount < it.size) {
                                    it.take(maxUsersCount)
                                } else {
                                    it
                                }
                            }
                            .map {
                                insertUsers(it, langInDb)
                                it
                            }
                            .doOnNext { users ->
                                if (users.size != QUERY_LIMIT) {
                                    subject.onComplete()
                                } else {
                                    if (maxUsersCount != null && maxUsersCount < users.size) {
                                        subject.onComplete()
                                    } else {
                                        subject.onNext(users.last().uid)
                                    }
                                }
                            }
                            //.doOnNext { println("users size: ${it.size}") }
                            .toList()
                            .map { Pair(lang, it.flatten()) }
                            .doOnSuccess { updateFirebaseUpdateDate(lang.lang) }
                }
                .subscribeBy(
                        onNext = {
                            log.error("done updating users for lang: ${it.first.lang}, totalCount: ${it.second.size}")
                            println("done updating users for lang: ${it.first.lang}, totalCount: ${it.second.size}")
                        },
                        onComplete = {
                            log.error("done updating users from firebase")
                            println("done updating users from firebase")
                        },
                        onError = {
                            println("error in update users observable: $it")
                            log.error("error in update users observable: ", it)
                        }
                )
    }

    fun getAllFirebaseUpdatedDataDates(): List<FirebaseDataUpdateDate> =
            firebaseDataUpdateDateRepository.findAll()

    @Transactional
    fun insertUsers(firebaseUsers: List<FirebaseUser>, lang: Lang) {
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
                                    username = it.email!!,
                                    password = it.email!!,
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
                    var userInDb = userService.getByUsername(userUidArticles.user.username)
                    if (userInDb == null) {
                        if (userUidArticles.user.avatar?.startsWith("data:image") == true) {
                            userUidArticles.user.avatar = ScpReaderConstants.DEFAULT_AVATAR_URL
//                            println("insert user with base64 avatar: ${userUidArticles.user}")
                        }
                        userInDb = userService.save(userUidArticles.user)

                        userToAuthorityService.save(UserToAuthority(userId = userInDb.id!!, authority = AuthorityType.USER))
                        newUsersInserted++
                    }

                    //add user-lang connection if need
//                    println("userInDb.id: ${userInDb.id}/${userInDb}")
                    if (usersLangsService.getByUserIdAndLangId(userInDb.id!!, lang.id) == null) {
//                        println("add user-lang connection if need: ${userInDb.id}/${lang.id}, ${userUidArticles.uid}")
                        usersLangsService.insert(UsersLangs(userId = userInDb.id!!, langId = lang.id, firebaseUid = userUidArticles.uid))
                        newLangForExistedUsers++
                    }

                    //increase score if need
                    val firebaseUser = userUidArticles.user
                    if (userInDb.score <= firebaseUser.score) {
                        userInDb.score = firebaseUser.score
                        //set level info
                        val curLevel = levelsJson.getLevelForScore(userInDb.score)!!
                        userInDb.levelNum = curLevel.id
                        userInDb.curLevelScore = curLevel.score
                        userInDb.scoreToNextLevel = levelsJson.scoreToNextLevel(userInDb.score, curLevel)
                        //update user in DB
                        userService.save(userInDb)
                    }

                    //insert read/favorite articles if need
                    manageFirebaseArticlesForUser(userUidArticles.articles, userInDb.id!!, lang)
                }

        println("newUsersInserted = $newUsersInserted, newLangForExistedUsers = $newLangForExistedUsers")
    }

    /**
     * inserts/updates read and favorite articles from firebase for user
     */
    fun manageFirebaseArticlesForUser(
            articlesInFirebase: List<FirebaseArticle>,
            userId: Long,
            lang: Lang
    ) {
//        println("manageFirebaseArticlesForUser lang/userId/articles.size: ${lang.id}/$userId/${articlesInFirebase.size}")
        articlesInFirebase.forEachIndexed { _, articleInFirebase ->
            if (articleInFirebase.url == null) {
//                println("manageFirebaseArticlesForUser: ${lang.id}/${user.username}")
//                println("articleInFirebase: $index/$articleInFirebase")
                return@forEachIndexed
            }
            val urlRelative = lang.removeDomainFromUrl(articleInFirebase.url!!)
            //for other langs we should not pass urlRelative
            val articleInDb = articleService.getArticleByUrlRelative(urlRelative)
//            println("articleInDb: $articleInDb $urlRelative")
            //insert new article and article-lang connection if need
            if (articleInDb == null) {
                //do nothing!
                return@forEachIndexed
            } else {
                //check if we do not have article-lang connection for given article
                val articleToLang: ArticleForLang? = try {
                    articleForLangService.getOneByLangIdAndArticleId(articleInDb.id!!, lang.id)
                } catch (e: Exception) {
                    if (e is IncorrectResultSizeDataAccessException) {
                        println("IncorrectResultSizeDataAccessException while get ArticleForLang: ${articleInDb.id}/$urlRelative")
                        log.error("IncorrectResultSizeDataAccessException while get ArticleForLangL ${articleInDb.id}/$urlRelative")
                        null
                    } else {
                        println("error while get ArticleForLang, ${e.message}")
                        log.error("error while get ArticleForLang", e)
                        null
                    }
                }

                if (articleToLang != null) {
                    articleInFirebase.isFavorite?.let { manageFavoriteArticlesForUserForLang(userId, it, articleToLang.id!!) }
                    articleInFirebase.isRead?.let { manageReadArticlesForUserForLang(userId, it, articleToLang.id!!) }
                }
            }
        }
    }

    private fun manageFavoriteArticlesForUserForLang(userId: Long, isFavorite: Boolean, articleToLangId: Long) {
        if (isFavorite) {
            val favoriteArticleForLang = favoriteArticleForLangService
                    .getFavoriteArticleForArticleIdLangIdAndUserId(userId = userId, articleToLangId = articleToLangId)
            if (favoriteArticleForLang == null) {
                favoriteArticleForLangService.save(
                        FavoriteArticleByLang(
                                userId = userId,
                                articleToLangId = articleToLangId
                        )
                )
            }
        }
    }

    private fun manageReadArticlesForUserForLang(userId: Long, isRead: Boolean, articleToLangId: Long) {
        if (isRead) {
            val readTransaction = transactionService
                    .findByTransactionTypeAndArticleToLangIdAndUserId(
                            transactionType = ScpReaderConstants.UserDataTransactionType.READ_ARTICLE,
                            articleToLangId = articleToLangId,
                            userId = userId
                    )
            if (readTransaction == null) {
                readArticleForLangService.addArticleToRead(
                        articleToLangId = articleToLangId,
                        userId = userId,
                        increaseScore = false
                )
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
//                    println("firebaseUsers: ${firebaseUsers?.map { it.email }}")
                    subscriber.onSuccess(firebaseUsers!!)
                }
            })
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

    companion object {
        const val QUERY_LIMIT = 100
    }
}

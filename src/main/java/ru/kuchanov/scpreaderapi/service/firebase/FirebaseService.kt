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
import ru.kuchanov.scpreaderapi.bean.articles.Article
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.FavoriteArticlesByLang
import ru.kuchanov.scpreaderapi.bean.auth.Authority
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseArticle
import ru.kuchanov.scpreaderapi.model.firebase.FirebaseUser
import ru.kuchanov.scpreaderapi.model.firebase.UserUidArticles
import ru.kuchanov.scpreaderapi.model.user.LevelsJson
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ArticleService
import ru.kuchanov.scpreaderapi.service.article.FavoriteArticleForLangService
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.UserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService
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

    fun getAllUsersForLang(langId: String) = userService.getAllUsersByLangId(langId)

    @Async
    fun test(lang: Constants.Firebase.FirebaseInstance) {
        val firebaseDatabase = FirebaseDatabase.getInstance(FirebaseApp.getInstance(lang.lang))

        val subject = BehaviorProcessor.createDefault("")

        subject
                .concatMap { startKey -> usersObservable(firebaseDatabase, startKey).toFlowable() }
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
                        onSuccess = { insertUsers(it, langService.getById(lang.lang)) },
                        onError = { println(it.message) }
                )
    }

    @Transactional
    private fun insertUsers(firebaseUsers: List<FirebaseUser>, lang: Lang) {
        println(firebaseUsers.size)

        var newUsersInserted = 0
        var newLangForExistedUsers = 0

        val levelsJson = LevelsJson.getLevelsJson()

//        println("levelsJson: $levelsJson")

        firebaseUsers
                .distinctBy { it.email }
                .map {
                    //set level info
//                    println("it.score: ${it.score}/${it.uid}")
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
                        userInDb = userService.insert(userUidArticles.user)
                        authorityService.insert(Authority(userInDb.id, "USER"))
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
        articlesInFirebase.forEach { articleInFirebase ->
            val urlRelative = articleInFirebase.url!!
            var articleInDb = articleService.getArticleByUrlRelativeAndLang(urlRelative, lang.id)
            //insert new article and article-lang connection if need
            if (articleInDb == null) {
                articleInDb = articleService.insert(Article())
                articleForLangService.insert(ArticleForLang(
                        articleId = articleInDb.id!!,
                        langId = lang.id,
                        urlRelative = articleInFirebase.url!!.replace("${lang.siteBaseUrl}/", ""),//todo
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
                    langId = lang.id
            ))
        }

        val dateInFirebase = articleInFirebase.updated
        val dateInDb = favoriteArticleForLang.updated!!.time

        //check timestamp and update if need
        if (dateInDb < dateInFirebase) {
            //outdated info in DB, so update it if value changed
            if (favoriteArticleForLang.isFavorite != articleInFirebase.isFavorite) {
                favoriteArticleForLang.isFavorite = articleInFirebase.isFavorite
                favoriteArticleForLangService.update(favoriteArticleForLang)
            }
        }
    }

    //todo create service and repository
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
            readArticleForLang = readArticleForLangService.insert(FavoriteArticlesByLang(
                    userId = user.id,
                    articleId = articleInDb.id,
                    langId = lang.id
            ))
        }

        val dateInFirebase = articleInFirebase.updated
        val dateInDb = readArticleForLang.updated!!.time

        //check timestamp and update if need
        if (dateInDb < dateInFirebase) {
            //outdated info in DB, so update it if value changed
            if (readArticleForLang.isRead != articleInFirebase.isRead) {
                readArticleForLang.isFavorite = articleInFirebase.isRead
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
                    val firebaseUsers = snapshot?.children?.map {
                        try {
                            it.getValue(FirebaseUser::class.java)
                        } catch (e: Exception) {
                            println(e)
                            print("\nKEY IS: ${it.key}\n")
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
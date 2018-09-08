package ru.kuchanov.scpreaderapi.service

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
import ru.kuchanov.scpreaderapi.bean.auth.Authority
import ru.kuchanov.scpreaderapi.bean.firebase.FirebaseUser
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UsersLangs
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.users.LangService
import ru.kuchanov.scpreaderapi.service.users.UserService
import ru.kuchanov.scpreaderapi.service.users.UsersLangsService

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

    fun getAllUsersForLang(langId: String) = langService.getAllUsersByLangId(langId)

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
                .toList()
                .map { it.flatten() }
                .subscribeBy(
                        onSuccess = { insertUsers(it, langService.getById(lang.lang)) },
                        onError = { println(it.message) }
                )
    }

    private fun insertUsers(firebaseUsers: List<FirebaseUser>, lang: Lang) {
        println(firebaseUsers.size)

        val users = firebaseUsers.map {
            User(
                    myUsername = it.email!!,
                    myPassword = it.email!!,
                    avatar = it.avatar,
                    userAuthorities = setOf(),
                    firebaseUid = it.uid,
                    fullName = it.fullName,
                    signInRewardGained = it.signInRewardGained,
                    score = it.score
            )
        }

        val usersInserted = userService.insert(users)

        usersInserted?.forEach {
            authorityService.insert(Authority(it.id, "USER"))
        }

        val usersLangs = usersInserted?.map {
            UsersLangs(
                    userId = it.id!!,
                    langId = lang.id
            )
        }

        usersLangsService.insert(usersLangs!!)
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
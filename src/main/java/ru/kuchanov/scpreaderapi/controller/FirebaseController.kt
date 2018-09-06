package ru.kuchanov.scpreaderapi.controller

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.bean.firebase.FirebaseUser
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.network.ModelConverter
import ru.kuchanov.scpreaderapi.service.FirebaseService


@RestController
@RequestMapping("/${Constants.FIREBASE_PATH}")
class FirebaseController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var apiClient: ApiClient

    @Autowired
    private lateinit var modelConverter: ModelConverter

//    @Autowired
//    private lateinit var firebaseApp: FirebaseApp

    @Autowired
    private lateinit var firebaseService: FirebaseService

    @GetMapping("/{lang}/test")
    fun test(@PathVariable(value = "lang") lang: Constants.Firebase.FirebaseInstance) {
        firebaseService.test(lang)
//        val firebaseApp = FirebaseDatabase.getInstance(FirebaseApp.getInstance(lang.lang))
//        val query = firebaseApp
//                .getReference("users")
//                .orderByChild(FirebaseUser.FIELD_SCORE)
//                .startAt(1.0)
//                .limitToLast(1000)
//        println("query: ${query.spec}")
//
//        query.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onCancelled(error: DatabaseError?) {
//                log.debug(error?.message)
//            }
//
//            override fun onDataChange(snapshot: DataSnapshot?) {
//                log.debug(snapshot?.children?.joinToString { ", " })
//            }
//        })
    }
}
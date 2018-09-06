package ru.kuchanov.scpreaderapi.service

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.bean.firebase.FirebaseUser

@Service
class FirebaseService {

    @Autowired
    private lateinit var log: Logger

    @Async
    fun test(lang: Constants.Firebase.FirebaseInstance){
        val firebaseApp = FirebaseDatabase.getInstance(FirebaseApp.getInstance(lang.lang))
        val query = firebaseApp
                .getReference("users")
                .orderByChild(FirebaseUser.FIELD_SCORE)
                .startAt(1.0)
                .limitToLast(1000)
        println("query: ${query.spec}")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError?) {
                log.debug(error?.message)
                println("error?.message: ${error?.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                log.debug(snapshot?.children?.joinToString { ", " })
                println("snapshot?.children?.joinToString { \", \" }: ${snapshot?.value}")
            }
        })
    }
}
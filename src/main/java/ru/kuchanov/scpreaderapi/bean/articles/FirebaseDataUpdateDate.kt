package ru.kuchanov.scpreaderapi.bean.articles

import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "firebase_data_update_date")
data class FirebaseDataUpdateDate(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "lang_id")
        val langId: String? = null,
        var updated: Timestamp? = null
)
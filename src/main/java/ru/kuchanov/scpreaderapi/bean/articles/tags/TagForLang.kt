package ru.kuchanov.scpreaderapi.bean.articles.tags

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(
        name = "tags_langs",
        uniqueConstraints = [
            javax.persistence.UniqueConstraint(
                    columnNames = ["lang_id", "title"]
            )
        ]
)
data class TagForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //relations
        @Column(name = "tag_id")
        var tagId: Long? = null,
        @Column(name = "lang_id")
        var langId: String,

        //content
        @Column(columnDefinition = "TEXT")
        var title: String?,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
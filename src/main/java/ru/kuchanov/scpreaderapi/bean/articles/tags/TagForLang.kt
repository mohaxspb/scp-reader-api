package ru.kuchanov.scpreaderapi.bean.articles.tags

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.sql.Timestamp
import javax.persistence.*


@Entity
@Table(name = "tags_langs")
@NoArgConstructor
data class TagForLang(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        //relations
        @Column(name = "tag_id")
        var tagId: Long,
        @Column(name = "lang_id")
        var langId: String,

        //content
        @Column(columnDefinition = "TEXT")
        var title: String,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
){
        companion object {
                const val NO_ID = -1L
        }
}

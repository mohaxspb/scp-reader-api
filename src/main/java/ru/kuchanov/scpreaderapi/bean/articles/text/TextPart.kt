package ru.kuchanov.scpreaderapi.bean.articles.text

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.model.dto.article.TextPartDto
import ru.kuchanov.scpreaderapi.service.parse.article.TextType
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_to_lang_text_parts")
data class TextPart(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "article_to_lang_id")
        var articleToLangId: Long? = null,
        @Column(name = "parent_id")
        var parentId: Long? = null,
        @Enumerated(EnumType.STRING)
        @Column(columnDefinition = "TEXT")
        val type: TextType,
        @Column(columnDefinition = "TEXT")
        val data: String?,
        @Column(name = "order_in_text")
        var orderInText: Int,

        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null,

        @Transient
        var innerTextParts: List<TextPart>? = null
) {
    override fun toString(): String {
        val shortenedData = if (data != null && data.length > 30) {
            data.substring(0, 30)
        } else {
            data
        }
        return "TextPart(type=$type, order=$orderInText, data=$shortenedData, inner=$innerTextParts)"
    }
}

fun TextPart.toDto() =
        TextPartDto(
                id = id!!,
                data = data,
                type = type
        )
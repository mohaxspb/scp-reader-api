package ru.kuchanov.scpreaderapi.model.dto.article

import java.sql.Timestamp


interface ReadOrFavoriteArticleProjection {
    val id: Long
    val statusChangedDate: Timestamp
    val articleToLangId: Long
    val articleId: Long
    val langId: String
    val urlRelative: String
    val title: String
    val rating: Int?
    val createdOnSite: Timestamp?
    val hasIframeTag: Boolean
}

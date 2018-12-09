package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.Article

interface ArticlesRepository : JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a " +
            "JOIN ArticleForLang al ON a.id = al.articleId " +
            "WHERE al.urlRelative = :urlRelative AND al.langId = :langId")
    fun getOneByUrlRelativeUrlAndLang(urlRelative: String, langId: String): Article?

    @Query("SELECT a FROM Article a  " +
            "JOIN ArticleForLang al ON a.id = al.articleId " +
            "WHERE al.urlRelative = :urlRelative")
    fun getArticleByUrlRelative(urlRelative: String): Article?

//    @Query("SELECT * FROM Article a " +
//            "JOIN Article_For_Lang al ON a.id = al.article_Id " +
//            "WHERE al.url_Relative = :urlRelative AND al.lang_Id = :langId", nativeQuery = true)
//    fun getOneByUrlRelativeAndLang(urlRelative: String, langId: String): Article?

//    @Query("SELECT al FROM ArticleForLang al " +
//            "WHERE al.urlRelative = :urlRelative AND al.langId = :langId")
//    fun getArticleForLangByUrlRelativeAndLang(urlRelative: String, langId: String): ArticleForLang?
}
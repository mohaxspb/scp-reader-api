package ru.kuchanov.scpreaderapi.repository.article

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.articles.Article

interface ArticlesRepository : JpaRepository<Article, Long> {

    @Query("""
        SELECT a FROM Article a 
        JOIN ArticleForLang al ON a.id = al.articleId 
        WHERE al.urlRelative = :urlRelative AND al.langId = :langId
        """)
    fun getOneByUrlRelativeUrlAndLang(urlRelative: String, langId: String): Article?

    @Query("""
        SELECT a FROM Article a 
        JOIN ArticleForLang al ON a.id = al.articleId 
        WHERE al.urlRelative = :urlRelative
        """)
    fun getArticleByUrlRelative(urlRelative: String): Article?

    @Query("""
        SELECT a FROM Article a 
        JOIN ArticleForLang al ON a.id = al.articleId 
        WHERE al.urlRelative = :urlRelative
        order by al.created desc
        """)
    fun getArticlesByUrlRelative(urlRelative: String): List<Article>

    @Query(
            """
            SELECT * FROM articles 
            WHERE created >= CAST( :startDate AS timestamp) 
            AND created <= CAST( :endDate AS timestamp) 
            ORDER BY created
        """,
            nativeQuery = true
    )
    fun getCreatedArticlesBetweenDates(startDate: String, endDate: String): List<Article>
}

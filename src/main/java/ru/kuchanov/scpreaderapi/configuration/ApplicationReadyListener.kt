package ru.kuchanov.scpreaderapi.configuration

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_ID
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.CATEGORIES_ARTICLES
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.users.LangService

@Component
class ApplicationReadyListener @Autowired constructor(
        private val articleForLangService: ArticleForLangService,
        private val langService: LangService,
        private val categoryForLangService: ArticleCategoryForLangService,
        private val cacheManager: CacheManager,
        private val log: Logger
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val categoriesArticlesCache = cacheManager.getCache(CATEGORIES_ARTICLES)
        log.error("categoriesArticlesCache populate start")
        ScpReaderConstants.Firebase.FirebaseInstance.values().forEach { langEnum ->
            log.error("categoriesArticlesCache populate articlesCategories $langEnum START")
            val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }
            categoryForLangService.findAllByLangId(lang.id).forEach categoryToLang@{ categoryToLang ->
                val articleCategoryToLang = categoryForLangService.findByLangIdAndArticleCategoryId(
                        langId = lang.id,
                        articleCategoryId = categoryToLang.articleCategoryId
                ) ?: return@categoryToLang

                val dataToPut = articleForLangService.findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLang.id!!)
                categoriesArticlesCache?.put(
                        SimpleKey(langEnum, categoryToLang.articleCategoryId),
                        dataToPut
                )
                log.error("categoriesArticlesCache $langEnum with text START")
                val articlesByIdCache = cacheManager.getCache(ARTICLE_TO_LANG_DTO_BY_ID)
                val articlesByLangAndUrlRelativeCache = cacheManager.getCache(ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG)
                //put articles with text parts
                val categoriesArticlesWithTextParts = articleForLangService
                        .findAllByIdsWithTextParts(dataToPut.map { it.id })
                categoriesArticlesWithTextParts.forEach {
                    articlesByIdCache?.put(SimpleKey(it.id), it)
                    articlesByLangAndUrlRelativeCache?.put(SimpleKey(langEnum, it.urlRelative), it)
                }

                log.error("categoriesArticlesCache $langEnum with text END")
            }

            log.error("categoriesArticlesCache populate articlesCategories $langEnum END")
        }
        log.error("categoriesArticlesCache populate articlesCategories END")
        val caffeinCache = (categoriesArticlesCache as CaffeineCache).nativeCache
        log.error("caffeinCahce estimatedSize: ${caffeinCache.estimatedSize()}")
        log.error("caffeinCahce requestCount: ${caffeinCache.stats().requestCount()}")
        log.error("caffeinCahce totalLoadTime: ${caffeinCache.stats().totalLoadTime()}")
    }
}
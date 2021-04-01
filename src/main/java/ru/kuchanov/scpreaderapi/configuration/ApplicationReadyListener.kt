package ru.kuchanov.scpreaderapi.configuration

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.CATEGORIES_ARTICLES
import ru.kuchanov.scpreaderapi.bean.articles.category.ArticleCategoryForLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryService
import ru.kuchanov.scpreaderapi.service.users.LangService
import com.github.benmanes.caffeine.cache.Cache as CacheImpl

@Component
class ApplicationReadyListener @Autowired constructor(
        private val articleForLangService: ArticleForLangService,
        private val langService: LangService,
        private val categoryForLangService: ArticleCategoryForLangService,
        private val categoryService: ArticleCategoryService,
        private val cacheManager: CacheManager,
        private val log: Logger
) : ApplicationListener<ApplicationReadyEvent> {

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val categoriesArticlesCache = cacheManager.getCache(CATEGORIES_ARTICLES)
        log.error("categoriesArticlesCache: $categoriesArticlesCache")
        ScpReaderConstants.Firebase.FirebaseInstance.values().forEach { langEnum ->
            val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }
            categoryForLangService.findAllByLangId(lang.id).forEach { categoryToLang ->
                val articleCategoryToLang = categoryForLangService.findByLangIdAndArticleCategoryId(
                        langId = lang.id,
                        articleCategoryId = categoryToLang.articleCategoryId
                )
                        ?: return@forEach
//                        ?: throw ArticleCategoryForLangNotFoundException("langId: ${lang.id}, ${categoryToLang.articleCategoryId}")

                val dataToPut = articleForLangService.findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLang.id!!)
                categoriesArticlesCache?.put(
                        SimpleKey(langEnum, categoryToLang.articleCategoryId),
                        dataToPut
                )
            }
        }
        log.error("categoriesArticlesCache: $categoriesArticlesCache")
//        val cmImpl = cacheManager as CaffeineCacheManager
//        cmImpl.getCache()
//        val caffeinCahce = categoriesArticlesCache as CacheImpl<*, *>
        val caffeinCahce = (categoriesArticlesCache as CaffeineCache).nativeCache
        log.error("caffeinCahce estimatedSize: ${caffeinCahce.estimatedSize()}")
        log.error("caffeinCahce requestCount: ${caffeinCahce.stats().requestCount()}")
        log.error("caffeinCahce totalLoadTime: ${caffeinCahce.stats().totalLoadTime()}")
    }
}
package ru.kuchanov.scpreaderapi.configuration

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.interceptor.SimpleKey
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_ID
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Cache.Keys.CATEGORIES_ARTICLES
import ru.kuchanov.scpreaderapi.bean.settings.ServerSettings
import ru.kuchanov.scpreaderapi.bean.settings.ServerSettingsNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.LangNotFoundException
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.category.ArticleCategoryForLangService
import ru.kuchanov.scpreaderapi.service.settings.ServerSettingsService
import ru.kuchanov.scpreaderapi.service.users.LangService

@Service
class CacheService @Autowired constructor(
        private val articleForLangService: ArticleForLangService,
        private val langService: LangService,
        private val categoryForLangService: ArticleCategoryForLangService,
        private val cacheManager: CacheManager,
        private val serverSettingsService: ServerSettingsService,
        private val log: Logger
) {

    @Async
    fun populateCache() {
        val categoriesArticlesCache = cacheManager.getCache(CATEGORIES_ARTICLES)
        val articlesByIdCache = cacheManager.getCache(ARTICLE_TO_LANG_DTO_BY_ID)
        val articlesByLangAndUrlRelativeCache = cacheManager.getCache(ARTICLE_TO_LANG_DTO_BY_URL_RELATIVE_AND_LANG)
        log.error("cache population START")
        val startTime = System.currentTimeMillis()
        ScpReaderConstants.Firebase.FirebaseInstance.values().forEach { langEnum ->
            val startTimeLang = System.currentTimeMillis()
            log.error("populate cache for $langEnum START")
            val lang = langEnum.lang.let { langService.getById(it) ?: throw LangNotFoundException() }
            categoryForLangService.findAllByLangId(lang.id).forEach categoryToLang@{ categoryToLang ->
                log.error("categoriesArticlesCache $langEnum ${categoryToLang.defaultTitle} START")
                val articleCategoryToLang = categoryForLangService.findByLangIdAndArticleCategoryId(
                        langId = lang.id,
                        articleCategoryId = categoryToLang.articleCategoryId
                ) ?: return@categoryToLang

                val dataToPut = articleForLangService.findAllArticlesForLangByArticleCategoryToLangId(articleCategoryToLang.id!!)
                categoriesArticlesCache?.put(
                        SimpleKey(langEnum, categoryToLang.articleCategoryId),
                        dataToPut
                )

                //put articles with text parts
                val categoriesArticlesWithTextParts = articleForLangService
                        .findAllByIdsWithTextParts(dataToPut.map { it.id })
                categoriesArticlesWithTextParts.forEach {
                    articlesByIdCache?.put(SimpleKey(it.id), it)
                    articlesByLangAndUrlRelativeCache?.put(SimpleKey(langEnum, it.urlRelative), it)
                }
                log.error("categoriesArticlesCache $langEnum ${categoryToLang.defaultTitle} END")
            }

            log.error("populate recent $langEnum with text START")
            //fill recent and popular articles with text cache
            val recentCacheInitialSize = serverSettingsService.findByKey(ServerSettings.Key.MOST_RECENT_ARTICLES_CACHE_SIZE.name)
                    ?: throw ServerSettingsNotFoundException("MOST_RECENT_ARTICLES_CACHE_SIZE not found!")

            val recentArticles = articleForLangService.getMostRecentArticlesForLangIds(
                    langEnum.lang,
                    0,
                    recentCacheInitialSize.value.toInt()
            )
            recentArticles.forEach {
                val articleWithText = articleForLangService.getOneByIdAsDto(it)!!
                articlesByIdCache?.put(SimpleKey(articleWithText.id), articleWithText)
                articlesByLangAndUrlRelativeCache?.put(SimpleKey(langEnum, articleWithText.urlRelative), articleWithText)
            }
            log.error("populate recent $langEnum with text END")
            log.error("populate rated $langEnum with text START")
            val ratedCacheInitialSize = serverSettingsService.findByKey(ServerSettings.Key.MOST_RATED_ARTICLES_CACHE_SIZE.name)
                    ?: throw ServerSettingsNotFoundException("MOST_RATED_ARTICLES_CACHE_SIZE not found!")

            val ratedArticles = articleForLangService.getMostRatedArticlesForLangIds(
                    langEnum.lang,
                    0,
                    ratedCacheInitialSize.value.toInt()
            )
            ratedArticles.forEach {
                val articleWithText = articleForLangService.getOneByIdAsDto(it)!!
                articlesByIdCache?.put(SimpleKey(articleWithText.id), articleWithText)
                articlesByLangAndUrlRelativeCache?.put(SimpleKey(langEnum, articleWithText.urlRelative), articleWithText)
            }
            log.error("populate rated $langEnum with text END")

            val (minutes, seconds) = millisToMinutesAndSeconds(System.currentTimeMillis() - startTimeLang)
            log.error("populate cache for $langEnum END. Total time (min:sec): $minutes:$seconds")
        }
        val (minutes, seconds) = millisToMinutesAndSeconds(System.currentTimeMillis() - startTime)
        log.error("cache population END. Total time (min:sec): $minutes:$seconds")

        val caffeinCategoriesArticlesCache = (categoriesArticlesCache as CaffeineCache).nativeCache
        log.error("caffeinCache estimatedSize: ${caffeinCategoriesArticlesCache.estimatedSize()}")

        val caffeineArticlesByIdCache = (articlesByIdCache as CaffeineCache).nativeCache
        log.error("caffeineArticlesByIdCache estimatedSize: ${caffeineArticlesByIdCache.estimatedSize()}")
    }

    private fun millisToMinutesAndSeconds(millis: Long): Pair<Long, Long> {
        val minutes = millis / 1000 / 60
        val seconds = millis / 1000 % 60
        return minutes to seconds
    }
}
package ru.kuchanov.scpreaderapi

import io.reactivex.Flowable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase
import ru.kuchanov.scpreaderapi.service.users.LangService

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class ArticlesListsParsingTest {

    @Autowired
    lateinit var articleParsingServiceBase: ArticleParsingServiceBase

    @Autowired
    lateinit var langService: LangService

    @Before
    fun prepare() {
        println("prepare start")
        println("prepare end")
    }

    val italy = ScpReaderConstants.Firebase.FirebaseInstance.IT

    @Test
    fun getRecentArticlesCount_returnsNotZeroForNotItalyLang() {
        Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                .map { langService.getById(it.lang)!! }
                .flatMapSingle {
                    articleParsingServiceBase
                            .getParsingRealizationForLang(it)
                            .getMostRecentArticlesPageCountForLang(it)
                }
                .doOnError { println(it) }
                .toList()
                .test()
                .assertValue { it[italy.ordinal] == 0 }
                .assertValue { pagesCount ->
                    val pagesCountWithoutItaly = pagesCount
                            .filterIndexed { index, _ -> index != italy.ordinal }
                            .filter { it != 0 }

                    val languagesWithoutItaly =
                            ScpReaderConstants.Firebase.FirebaseInstance.values()
                                    .filter { it.lang != italy.lang }

                    pagesCountWithoutItaly.size == languagesWithoutItaly.size
                }
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getRecentArticles_receivesNotEmptyListForNotItalyLang() {
        Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                .map { langService.getById(it.lang)!! }
                .flatMapSingle { lang ->
                    articleParsingServiceBase
                            .getParsingRealizationForLang(lang)
                            .getRecentArticlesForPage(lang, 1)
                }
                .doOnError { println(it) }
                .toList()
                .test()
                .assertValue { it[italy.ordinal].isEmpty() }
                .assertValue { allArticlesForLangs ->
                    val notEmptyArticlesListsWithoutItaly =
                            allArticlesForLangs
                                    .filterIndexed { index, _ -> index != italy.ordinal }
                                    .filter { it.isNotEmpty() }
                    val languagesWithoutItaly =
                            ScpReaderConstants.Firebase.FirebaseInstance.values()
                                    .filter { it.lang != italy.lang }
                    notEmptyArticlesListsWithoutItaly.size == languagesWithoutItaly.size
                }
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getRatedArticles_receivesNotEmptyList() {
        Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                .map { langService.getById(it.lang)!! }
                .flatMapSingle { lang ->
                    articleParsingServiceBase
                            .getParsingRealizationForLang(lang)
                            .getRatedArticlesForLang(lang, 1)
                }
                .doOnError { println(it) }
                .toList()
                .test()
                .assertValue { allArticlesForLangs ->
                    val notEmptyArticlesLists = allArticlesForLangs.filter { it.isNotEmpty() }
                    val allLangs = ScpReaderConstants.Firebase.FirebaseInstance.values()
                    notEmptyArticlesLists.size == allLangs.size
                }
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun getObjectArticles_receivesNotEmptyList() {
        Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                //todo remove filter after all lang realization
                .filter {
                    it == ScpReaderConstants.Firebase.FirebaseInstance.RU
                            || it == ScpReaderConstants.Firebase.FirebaseInstance.EN
                }
                .map { langService.getById(it.lang)!! }
                .flatMapSingle { lang ->
                    val objectUrls = articleParsingServiceBase
                            .getParsingRealizationForLang(lang)
                            .getObjectArticlesUrls()
                            .map { Pair(lang, it) }
                    Flowable
                            .fromIterable(objectUrls)
                            .flatMapSingle { langAndObjectUrl ->
                                articleParsingServiceBase
                                        .getParsingRealizationForLang(langAndObjectUrl.first)
                                        .getObjectsArticlesForLang(langAndObjectUrl.first, langAndObjectUrl.second)
                                        .map { Triple(langAndObjectUrl.first, langAndObjectUrl.second, it) }
                            }
                            .toList()
                }
                .doOnError { println(it) }
                .toList()
                .doOnSuccess { println("doOnSuccess ${it.size}") }
                .test()
                //assert that every lang has at least some articles for every object link
                .assertValue { listOfObjectListsForEveryLang: MutableList<MutableList<Triple<Lang, String, List<ArticleForLang>>>> ->
                    val notEmptyArticlesLists =
                            listOfObjectListsForEveryLang
                                    //remove if no object urls were found
                                    .filter { it.isNotEmpty() }
                                    //remove if no articles found for object url for this lang
                                    .filter { listOfAllObjectArticleForLang: MutableList<Triple<Lang, String, List<ArticleForLang>>> ->
                                        val notEmptyObjectsListsForLang =
                                                listOfAllObjectArticleForLang
                                                        .filter { langAndObjectUrlAndArticles: Triple<Lang, String, List<ArticleForLang>> ->
                                                            val lang = langAndObjectUrlAndArticles.first.langCode
                                                            val url = langAndObjectUrlAndArticles.second
                                                            val articlesCount = langAndObjectUrlAndArticles.third.size
                                                            println("Articles for $lang ($url): $articlesCount")
                                                            langAndObjectUrlAndArticles.second.isNotEmpty()
                                                        }
                                        val numOfObjectLinks = articleParsingServiceBase
                                                .getParsingRealizationForLang(listOfAllObjectArticleForLang.first().first)
                                                .getObjectArticlesUrls().size
                                        println("numOfObjectLinks: $numOfObjectLinks")
                                        println("notEmptyObjectsListsForLang: ${notEmptyObjectsListsForLang.size}")
                                        notEmptyObjectsListsForLang.size == numOfObjectLinks
                                    }
                    val allLangs = ScpReaderConstants.Firebase.FirebaseInstance.values()
                            //fixme add other langs
                            .filter {
                                it == ScpReaderConstants.Firebase.FirebaseInstance.RU
                                        || it == ScpReaderConstants.Firebase.FirebaseInstance.EN
                            }
                    notEmptyArticlesLists.size == allLangs.size
                }
                .assertNoErrors()
                .assertComplete()
    }
}

package ru.kuchanov.scpreaderapi

import io.reactivex.Flowable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
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
    fun getRecentArticlesCountReturnsSomething() {
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
}

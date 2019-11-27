package ru.kuchanov.scpreaderapi

import io.reactivex.Flowable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import ru.kuchanov.scpreaderapi.service.parse.ArticleParsingServiceBase
import ru.kuchanov.scpreaderapi.service.users.LangService

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class ArticlesListsParsingTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var articleParsingServiceBase: ArticleParsingServiceBase

    @Autowired
    lateinit var langService: LangService

    @Before
    fun prepare() {
        println("prepare start")
        println("prepare end")
    }

    @Test
    fun getRecentArticlesCountReturnsSomething() {
        val listOfPagesCount = Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                .map { langService.getById(it.lang)!! }
                .flatMapSingle {
                    articleParsingServiceBase
                            .getParsingRealizationForLang(it)
                            .getMostRecentArticlesPageCountForLang(it)
                }
                //todo test as rx. Check italy
                .toList()
                .blockingGet()

        assertThat(listOfPagesCount).isNotEmpty
    }

    @Test
    fun getRecentArticles_receivesNotEmptyListForNotItalyLang() {
        val italy = ScpReaderConstants.Firebase.FirebaseInstance.IT

        Flowable
                .fromIterable(ScpReaderConstants.Firebase.FirebaseInstance.values().toList())
                .map { langService.getById(it.lang)!! }
                .flatMapSingle { lang ->
                    articleParsingServiceBase
                            .getParsingRealizationForLang(lang)
                            .getRecentArticlesForPage(lang, 1)
                            .map { Pair(it, lang) }
                }
                .doOnError { println(it) }
                .toList()
                .test()
                .assertValue {
                    it[italy.ordinal].second.langCode == italy.lang
                            && it[italy.ordinal].first.isEmpty()
                }
                .assertValue { allArticlesForLangs ->
                    val notEmptyArticlesListsWithoutItaly =
                            allArticlesForLangs
                                    .filter { it.second.langCode != italy.lang }
                                    .filter { it.first.isNotEmpty() }
                    val languagesWithoutItaly =
                            ScpReaderConstants.Firebase.FirebaseInstance.values()
                                    .filter { it.lang != italy.lang }
                    notEmptyArticlesListsWithoutItaly.size == languagesWithoutItaly.size
                }
                .assertNoErrors()
                .assertComplete()
    }
}

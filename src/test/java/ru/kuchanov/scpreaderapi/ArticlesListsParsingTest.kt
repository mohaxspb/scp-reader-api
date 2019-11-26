package ru.kuchanov.scpreaderapi

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class ArticlesListsParsingTest {

    @Autowired
    lateinit var mvc: MockMvc

    @Before
    fun prepare() {
        println("prepare start")
        println("prepare end")
    }

    @Test
    fun securedUrlWithoutAccessToken_redirectsToLogin() {
        mvc
                .perform(
                        MockMvcRequestBuilders.get("/" + ScpReaderConstants.Path.ARTICLE + "/RU/object/all?processOnlyCount=3")
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful)
    }
}
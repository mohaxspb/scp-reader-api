package ru.kuchanov.scpreaderapi.service.parse

import ru.kuchanov.scpreaderapi.bean.users.Lang

interface ArticleParsingService {

    fun parseMostRecentArticlesForLang(lang: Lang, maxPageCount: Int? = null)
}
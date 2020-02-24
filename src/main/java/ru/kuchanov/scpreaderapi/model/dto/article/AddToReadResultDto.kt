package ru.kuchanov.scpreaderapi.model.dto.article

import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang

data class AddToReadResultDto(
        val readArticleByLang: ReadArticleByLang,
        val scoreAdded: Int
)
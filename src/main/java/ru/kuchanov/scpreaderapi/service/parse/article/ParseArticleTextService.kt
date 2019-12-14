package ru.kuchanov.scpreaderapi.service.parse.article

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Service
import java.util.*

@Service
class ParseArticleTextService {

    fun parseArticleText(rawText: String, printTextParts: Boolean) {
        val textParts = mutableListOf<String>()
        val rawTextParts = getArticlesTextParts(rawText)
        for (value in rawTextParts) {
            textParts.add(value)
        }
        val textPartsTypes = mutableListOf<TextType>()
        for (value in getListOfTextTypes(rawTextParts)) {
            textPartsTypes.add(value)
        }

        if (printTextParts) {
            println("textParts: ${textParts.size}")
            println("textPartsTypes: ${textPartsTypes.size}\n")
            textParts.forEachIndexed { index, value ->
                println("$index: ${textPartsTypes[index]}\n")
                println("$index: $value\n\n")
            }
        }
    }

    private fun getArticlesTextParts(html: String): List<String> {
        val document = Jsoup.parse(html)
        var contentPage: Element? = document.getElementById(ParseConstants.ID_PAGE_CONTENT)
        if (contentPage == null) {
            contentPage = document.body()
        }
        val articlesTextParts = ArrayList<String>()
        for (element in contentPage!!.children()) {
            articlesTextParts.add(element.outerHtml())
        }
        return articlesTextParts
    }

    private fun getListOfTextTypes(articlesTextParts: Iterable<String>): List<TextType> {
        val listOfTextTypes = mutableListOf<TextType>()
        for (textPart in articlesTextParts) {
            val element = Jsoup.parse(textPart)
            val ourElement = element.getElementsByTag(ParseConstants.TAG_BODY).first().children().first()
            when {
                ourElement == null -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == ParseConstants.TAG_P -> listOfTextTypes.add(TextType.TEXT)
                ourElement.className() == ParseConstants.CLASS_SPOILER -> listOfTextTypes.add(TextType.SPOILER)
                ourElement.classNames().contains(ParseConstants.CLASS_TABS) -> listOfTextTypes.add(TextType.TABS)
                ourElement.tagName() == ParseConstants.TAG_TABLE -> listOfTextTypes.add(TextType.TABLE)
                ourElement.className() == "rimg"
                        || ourElement.className() == "limg"
                        || ourElement.className() == "cimg"
                        || ourElement.classNames().contains("scp-image-block") -> listOfTextTypes.add(TextType.IMAGE)
                else -> listOfTextTypes.add(TextType.TEXT)
            }
        }

        return listOfTextTypes
    }
}
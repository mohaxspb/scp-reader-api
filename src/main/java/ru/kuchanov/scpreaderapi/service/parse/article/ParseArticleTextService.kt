package ru.kuchanov.scpreaderapi.service.parse.article

import com.fasterxml.jackson.databind.ObjectMapper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import java.util.*


@Service
class ParseArticleTextService @Autowired constructor(
        val objectMapper: ObjectMapper
) {

    fun parseArticleText(rawText: String, printTextParts: Boolean): List<TextPart> {
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

        val finalTextParts = mutableListOf<TextPart>()

        var order = 0
        for (index in 0 until textParts.size) {
            val textPart = textParts[index]
            @Suppress("MoveVariableDeclarationIntoWhen")
            val textPartType = textPartsTypes[index]

            when (textPartType) {
                TextType.SPOILER -> {
                    val spoilerData = parseSpoilerParts(textPart)
                    val titles = listOf(spoilerData.collapsedTitle, spoilerData.expandedTitle)
                    val titlesJson = objectMapper.writeValueAsString(titles)
                    val spoilerTextPart = TextPart(data = titlesJson, type = TextType.SPOILER, orderInText = order++)
                    spoilerTextPart.innerTextParts = parseArticleText(spoilerData.spoilerData, printTextParts)
                    finalTextParts += spoilerTextPart
                }
                TextType.TEXT -> finalTextParts += TextPart(
                        data = textPart,
                        type = textPartType,
                        orderInText = order++
                )
                TextType.IMAGE -> {
                    finalTextParts += parseImageData(textPart, order++)
                }
                TextType.TABLE -> {
                    //todo
                }
                TextType.TABS -> {
                    //todo
                }
            }
        }

        return finalTextParts
    }

    private fun parseImageData(data: String, order: Int): TextPart {
        val document = Jsoup.parse(data)
        val imageTag = document.getElementsByTag("img").first()
        val imageUrl = imageTag?.attr("src")

        val spans = document.getElementsByTag("span")
        val scpImageCaptions = document.getElementsByClass("scp-image-caption")
        val title = when {
            !spans.isEmpty() -> spans.html()
            !scpImageCaptions.isEmpty() -> scpImageCaptions.first().html()
            else -> null
        }

        return TextPart(
                data = objectMapper.writeValueAsString(ImageData(imageUrl, title)),
                type = TextType.IMAGE,
                orderInText = order
        )
    }

    private fun parseSpoilerParts(html: String): SpoilerData {
//        Timber.d("parseSpoilerParts: %s", html);
        val document: Document = Jsoup.parse(html)
        val element = document.getElementsByClass("collapsible-block-folded").first()
        val elementA = element.getElementsByTag("a").first()
        //replacing non-breaking-spaces
        val collapsedTitle = elementA.text().replace("\\p{Z}".toRegex(), " ")
        println("spoilerParts: $collapsedTitle")
        val elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first()
        val elementExpanded = elementUnfolded.getElementsByClass("collapsible-block-link").first()

        val expandedTitle = elementExpanded.text().replace("\\p{Z}".toRegex(), " ")
        println("spoilerParts: $expandedTitle")
//        Timber.d("elementUnfolded.children().size(): %s", elementUnfolded.children().size());
//        Timber.d("elementUnfolded.children().get(1).hasText(): %s", elementUnfolded.children().get(1).hasText());
//        Timber.d("elementUnfolded: %s", elementUnfolded.html());
        val elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first()
        val spoilerData: String
        if (elementContent != null && elementContent.hasText()) {
            println("elementContent != null && elementContent.hasText()")
            unwrapTextAlignmentDivs(elementContent)
            spoilerData = elementContent.html()
        } else if (elementContent != null && !elementContent.hasText()
                && elementUnfolded.children().size > 2 && elementUnfolded.children()[2].hasText()) {
            println("elementContent != null\n" +
                    "                && !elementContent.hasText()\n" +
                    "                && elementUnfolded.children().size() > 1\n" +
                    "                && elementUnfolded.children().get(1).hasText()")
            elementExpanded.parent().remove()
            elementContent.remove()
            unwrapTextAlignmentDivs(elementUnfolded.children().first())
            spoilerData = elementUnfolded.html()
        } else {
            println("ERROR WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)")
            spoilerData = "ERROR WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)"
        }
        return SpoilerData(collapsedTitle, expandedTitle, spoilerData)
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

    //todo use it in article
    private fun unwrapTextAlignmentDivs(element: Element) {
        val children: Elements = element.children()
        if (element.tagName() == "div" && element.hasAttr("style")
                && element.attr("style").contains("text-align")) {
            element.unwrap()
        }
        if (element.tagName() == "div" && element.hasAttr("style")
                && element.attr("style").contains("float")
                && element.className() != "scp-image-block") {
            element.unwrap()
        }
        for (child in children) {
            unwrapTextAlignmentDivs(child)
        }
    }

    private data class SpoilerData(
            val collapsedTitle: String,
            val expandedTitle: String,
            val spoilerData: String
    )

    private data class ImageData(
            val url: String?,
            val title: String?
    )
}
package ru.kuchanov.scpreaderapi.service.parse.article

import com.fasterxml.jackson.databind.ObjectMapper
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.CLASS_SPOILER
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.CLASS_TABS
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_BLOCKQUOTE
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_LI
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE


@Service
class ParseArticleTextService @Autowired constructor(
        val objectMapper: ObjectMapper
) {

    fun parseArticleText(rawText: String, printTextParts: Boolean): List<TextPart> {
        val textParts = getArticlesTextParts(rawText)
        val textPartsTypes = getListOfTextTypes(getArticlesTextParts(rawText))

//        if (printTextParts) {
//            println("textParts: ${textParts.size}")
//            println("textPartsTypes: ${textPartsTypes.size}\n")
//            textParts.forEachIndexed { index, value ->
//                println("$index: ${textPartsTypes[index]}\n")
//                println("$index: $value\n\n")
//            }
//        }

        val finalTextParts = mutableListOf<TextPart>()

        var order = 0
        for (index in textParts.indices) {
            val textPart = textParts[index]
            finalTextParts += when (textPartsTypes[index]) {
                TextType.SPOILER -> parseSpoilerParts(textPart, order++)
                TextType.IMAGE -> parseImageData(textPart, order++)
                TextType.TABLE -> parseTable(textPart, order++)
                TextType.BLOCKQUOTE -> parseBlockquote(textPart, order++)
                TextType.TABS -> parseTabs(textPart, order++)
                /*TextType.TEXT*/
                else -> TextPart(data = textPart, type = TextType.TEXT, orderInText = order++)
            }
        }

        return finalTextParts
    }

    private fun parseBlockquote(textPart: String, order: Int): TextPart {
        val blockquoteTextPart = TextPart(data = null, type = TextType.BLOCKQUOTE, orderInText = order)

        val document = Jsoup.parse(textPart)
        val extractedFromBlockquote = document.getElementsByTag("blockquote").first().html()

        blockquoteTextPart.innerTextParts = parseArticleText(extractedFromBlockquote, false)

        return blockquoteTextPart
    }

    private fun parseTable(textPart: String, order: Int) =
            TextPart(
                    data = createTableHtml(textPart),
                    type = TextType.TABLE,
                    orderInText = order
            )

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

    private fun parseSpoilerParts(html: String, order: Int): TextPart {
        val spoilerTextPart = TextPart(data = null, type = TextType.SPOILER, orderInText = order)

        val document = Jsoup.parse(html)

        //parse collapsed part
        val element = document.getElementsByClass("collapsible-block-folded").first()
        val elementA = element.getElementsByTag("a").first()
        //replacing non-breaking-spaces
        val collapsedTitle = elementA.text().replace("\\p{Z}".toRegex(), " ")
        val collapsedSpoilerTextPart = TextPart(data = collapsedTitle, type = TextType.SPOILER_COLLAPSED, orderInText = 0)

        //parse expanded part
        val elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first()
        val elementExpanded = elementUnfolded.getElementsByClass("collapsible-block-link").first()

        val expandedTitle = elementExpanded.text().replace("\\p{Z}".toRegex(), " ")
        val expandedSpoilerTextPart = TextPart(data = expandedTitle, type = TextType.SPOILER_EXPANDED, orderInText = 1)

        //parse spoiler text
        val elementContent = elementUnfolded.getElementsByClass("collapsible-block-content").first()
        val spoilerData = if (elementContent != null && elementContent.hasText()) {
            elementContent.html()
        } else if (elementContent != null && !elementContent.hasText()
                && elementUnfolded.children().size > 2 && elementUnfolded.children()[2].hasText()) {
            elementExpanded.parent().remove()
            elementContent.remove()
            elementUnfolded.html()
        } else {
            throw IllegalStateException("ERROR WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)")
        }

        //combine data
        expandedSpoilerTextPart.innerTextParts = parseArticleText(spoilerData, false)
        spoilerTextPart.innerTextParts = listOf(collapsedSpoilerTextPart, expandedSpoilerTextPart)
        return spoilerTextPart
    }

    fun parseTabs(html: String, order: Int): TextPart {
        val document = Jsoup.parse(html)
        val yuiNavset = document.getElementsByClass(CLASS_TABS).first()
        yuiNavset?.let { tabsElement ->
            val titlesTag = tabsElement.getElementsByClass("yui-nav").first()
            val liElements = titlesTag.getElementsByTag(TAG_LI)
            val tabsTitles = liElements.map { it.text() }

            val tabsTextPart = TextPart(data = null, type = TextType.TABS, orderInText = order)

            val yuiContent = tabsElement.getElementsByClass("yui-content").first()
            var tabOrder = 0

            tabsTextPart.innerTextParts = yuiContent.children().mapIndexed { index, element ->
                val tabTextPart = TextPart(data = tabsTitles[index], type = TextType.TAB, orderInText = tabOrder++)
                tabTextPart.innerTextParts = parseArticleText(element.html(), false)
                tabTextPart
            }

            return tabsTextPart
        } ?: throw IllegalArgumentException("error parse tabs")
    }

    private fun getArticlesTextParts(html: String): List<String> {
        val document = Jsoup.parse(html)
        val contentPage = document.getElementById(ParseConstants.ID_PAGE_CONTENT) ?: document.body()
        return contentPage!!.children().map { it.outerHtml() }
    }

    private fun getListOfTextTypes(articlesTextParts: Iterable<String>): List<TextType> {
        val listOfTextTypes = mutableListOf<TextType>()
        for (textPart in articlesTextParts) {
            val element = Jsoup.parse(textPart)
            val ourElement = element.getElementsByTag(ParseConstants.TAG_BODY).first().children().first()
            when {
                ourElement == null -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == TAG_P -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == TAG_BLOCKQUOTE -> listOfTextTypes.add(TextType.BLOCKQUOTE)
                ourElement.tagName() == TAG_TABLE -> listOfTextTypes.add(TextType.TABLE)
                ourElement.className() == CLASS_SPOILER -> listOfTextTypes.add(TextType.SPOILER)
                ourElement.classNames().contains(CLASS_TABS) -> listOfTextTypes.add(TextType.TABS)
                ourElement.className() == "rimg"
                        || ourElement.className() == "limg"
                        || ourElement.className() == "cimg"
                        || ourElement.classNames().contains("scp-image-block") -> listOfTextTypes.add(TextType.IMAGE)
                else -> listOfTextTypes.add(TextType.TEXT)
            }
        }

        return listOfTextTypes
    }

    private data class ImageData(
            val url: String?,
            val title: String?
    )

    companion object {
        private const val TABLE_TEXT_COLOR_VAR_NAME = "TABLE_TEXT_COLOR_VAR_NAME"
        private const val TABLE_BACKGROUND_COLOR_VAR_NAME = "TABLE_BACKGROUND_COLOR_VAR_NAME"

        private fun createTableHtml(tableData: String) = """
             |<!DOCTYPE html>
             |  <html>
             |      <head>
             |          <meta charset="utf-8">
             |          <meta name="viewport" content="width=device-width, user-scalable=yes" />
             |          <style>
             |              table.wiki-content-table{border-collapse:collapse;border-spacing:0;margin:.5em auto}
             |              table.wiki-content-table td{border:1px solid $TABLE_TEXT_COLOR_VAR_NAME;color: $TABLE_TEXT_COLOR_VAR_NAME;padding:.3em .7em;background-color: $TABLE_BACKGROUND_COLOR_VAR_NAME}
             |              table.wiki-content-table th{border:1px solid $TABLE_TEXT_COLOR_VAR_NAME;color: $TABLE_TEXT_COLOR_VAR_NAME;padding:.3em .7em;background-color: $TABLE_BACKGROUND_COLOR_VAR_NAME}
             |          </style>
             |      </head>
             |      <body>$tableData</body>
             |  </html>
           """.trimMargin()
    }
}

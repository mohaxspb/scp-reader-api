package ru.kuchanov.scpreaderapi.service.parse.article

import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_SRC
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.CLASS_SPOILER
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.CLASS_TABS
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ID_PAGE_CONTENT
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_BLOCKQUOTE
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_BODY
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_HR
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_IMG
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_LI
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_SPAN
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE
import ru.kuchanov.scpreaderapi.service.parse.category.ScpParseException


@Service
class ParseArticleTextService {

    /**
     * @throws ScpParseException
     */
    fun parseArticleText(rawText: String, printTextParts: Boolean = false): List<TextPart> {
        val textParts = getArticlesTextParts(rawText)
        //println("textParts: ${textParts.size}")
        val textPartsTypes = getListOfTextTypes(textParts)
        //println("textPartsTypes: ${textPartsTypes.size}")

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
                TextType.HR -> TextPart(data = null, type = TextType.HR, orderInText = order++)
                /*TextType.TEXT*/
                //do not use inner textTypes, such as ImageUrl, Tab, SpoilerCollapsed and so on.
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
        val imageTag = document.getElementsByTag(TAG_IMG).first()
        val imageUrl = imageTag?.attr(ATTR_SRC)

        val spans = document.getElementsByTag(TAG_SPAN)
        val scpImageCaptions = document.getElementsByClass("scp-image-caption")
        val title = when {
            !spans.isEmpty() -> spans.html()
            !scpImageCaptions.isEmpty() -> scpImageCaptions.first().html()
            else -> null
        }

        val imageTextPart = TextPart(data = null, type = TextType.IMAGE, orderInText = order)
        val imageUrlTextPart = TextPart(data = imageUrl, type = TextType.IMAGE_URL, orderInText = 0)
        val imageTitleTextPart = TextPart(data = title, type = TextType.IMAGE_TITLE, orderInText = 1)
        imageTextPart.innerTextParts = listOf(imageUrlTextPart, imageTitleTextPart)

        return imageTextPart
    }

    private fun parseSpoilerParts(html: String, order: Int): TextPart {
        val spoilerTextPart = TextPart(data = null, type = TextType.SPOILER, orderInText = order)

        val document = Jsoup.parse(html)

        //println("document: $document")
        //parse collapsed part
        val elementFolded = document.getElementsByClass("collapsible-block-folded").first()
        val elementA = elementFolded.getElementsByTag("a").first()
        //replacing non-breaking-spaces
        val collapsedTitle = elementA.text().replace("\\p{Z}".toRegex(), " ")
        val collapsedSpoilerTextPart = TextPart(data = collapsedTitle, type = TextType.SPOILER_COLLAPSED, orderInText = 0)

        //parse expanded part
        val elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first()
        val elementUnfoldedBlockLink = elementUnfolded.getElementsByClass("collapsible-block-unfolded-link").first()
        val elementUnfoldedLink = elementUnfolded.getElementsByClass("collapsible-block-link").first()

        val expandedTitle = elementUnfoldedLink.text().replace("\\p{Z}".toRegex(), " ")
        val expandedSpoilerTextPart = TextPart(data = expandedTitle, type = TextType.SPOILER_EXPANDED, orderInText = 1)

        //parse spoiler text
        val elementUnfoldedContent = elementUnfolded.getElementsByClass("collapsible-block-content").first()

        val spoilerData: String
        if (elementUnfoldedContent != null) {
            //in /scp-1412 there is only IMG in content... or image div in /scp-1672
            if (elementUnfoldedContent.hasText() || elementUnfoldedContent.children().isNotEmpty()) {
                spoilerData = elementUnfoldedContent.html()
            } else {
                throw ScpParseException("ERROR 0 WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)")
            }
        } else {
            //see spoilers in articles `/scp-3003`, `/scp-323` and others
            //there is no content tag, as we previously remove all empty divs
            @Suppress("LiftReturnOrAssignment")
            if (elementUnfolded.children().size > 1) {
                //remove link div. Others - content.
                elementUnfoldedLink.remove()
                elementUnfoldedBlockLink?.remove()
                spoilerData = elementUnfolded.html()
            } else {
                //in some articles there is really no data in spoiler...
                //so return empty string...
                //see `/scp-2747`
                println("NO DATA IN SPOILER!")
                spoilerData = ""
                //throw IllegalStateException("ERROR 1 WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)")
            }
        }

        //combine data
        expandedSpoilerTextPart.innerTextParts = parseArticleText(spoilerData, false)
        spoilerTextPart.innerTextParts = listOf(collapsedSpoilerTextPart, expandedSpoilerTextPart)
        return spoilerTextPart
    }

    private fun parseTabs(html: String, order: Int): TextPart {
        val document = Jsoup.parse(html)
        val yuiNavset = document.getElementsByClass(CLASS_TABS).first()
        yuiNavset?.let { tabsElement ->
            val titlesTag = tabsElement.getElementsByClass("yui-nav").first()
            val liElements = titlesTag.getElementsByTag(TAG_LI)
            val tabsTitles = liElements.map { it.text() }

            val tabsTextPart = TextPart(data = null, type = TextType.TABS, orderInText = order)

            val yuiContent = tabsElement.getElementsByClass("yui-content").first()
            var tabOrder = 0

            //println("Parse tabs: $yuiContent")
            tabsTextPart.innerTextParts = yuiContent.children().mapIndexed { index, element ->
                val tabTextPart = TextPart(data = tabsTitles[index], type = TextType.TAB, orderInText = tabOrder++)
                tabTextPart.innerTextParts = parseArticleText(element.html(), false)
                tabTextPart
            }

            return tabsTextPart
        } ?: throw ScpParseException("error parse tabs")
    }

    private fun getArticlesTextParts(html: String): List<String> {
        //println("getArticlesTextParts: $html")
        val document = Jsoup.parse(html)
        val contentPage = document.getElementById(ID_PAGE_CONTENT) ?: document.body()
        return contentPage!!.children().map { it.outerHtml() }
    }

    private fun getListOfTextTypes(articlesTextParts: Iterable<String>): List<TextType> {
        val listOfTextTypes = mutableListOf<TextType>()
        for (textPart in articlesTextParts) {
            val element = Jsoup.parse(textPart)
            val ourElement = element.getElementsByTag(TAG_BODY).first().children().first()
            when {
                ourElement == null -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == TAG_P -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == TAG_HR -> listOfTextTypes.add(TextType.HR)
                ourElement.tagName() == TAG_BLOCKQUOTE -> listOfTextTypes.add(TextType.BLOCKQUOTE)
                ourElement.tagName() == TAG_TABLE -> listOfTextTypes.add(TextType.TABLE)
                ourElement.className() == CLASS_SPOILER -> listOfTextTypes.add(TextType.SPOILER)
                ourElement.classNames().contains(CLASS_TABS) -> listOfTextTypes.add(TextType.TABS)
                ourElement.className() == "rimg"
                        || ourElement.className() == "limg"
                        || ourElement.className() == "cimg"
                        || ourElement.className() == "image"
                        //see /operation-overmeta
                        || (ourElement.tagName() == TAG_A && ourElement.children().size == 1 && ourElement.children().first().tagName() == TAG_IMG)
                        || ourElement.classNames().contains("image-container")
                        || ourElement.classNames().contains("scp-image-block") -> listOfTextTypes.add(TextType.IMAGE)
                else -> listOfTextTypes.add(TextType.TEXT)
            }
        }

        return listOfTextTypes
    }

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

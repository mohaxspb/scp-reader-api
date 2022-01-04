package ru.kuchanov.scpreaderapi.service.parse.article

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.Application
import ru.kuchanov.scpreaderapi.bean.articles.text.TextPart
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
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
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_OL
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_P
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_SPAN
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_UL
import ru.kuchanov.scpreaderapi.service.parse.category.ScpParseException


@Service
class ParseArticleTextService @Autowired constructor(
        @Qualifier(Application.PARSING_LOGGER) private val log: Logger
) {

    /**
     * @throws ScpParseException
     */
    fun parseArticleText(rawText: String, printTextParts: Boolean = false): List<TextPart> {
        val textParts = getArticlesTextParts(rawText)
        //log.debug("textParts: ${textParts.size}")
        val textPartsTypes = getListOfTextTypes(textParts)
        //log.debug("textPartsTypes: ${textPartsTypes.size}")

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
                TextType.BULLET_LIST, TextType.NUMBERED_LIST -> parseList(textPart, order++, textPartsTypes[index])
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
        val extractedFromBlockquote = document.getElementsByTag("blockquote").first()!!.html()

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
        //see `scp-1267`
        val trTags = document.getElementsByTag("tr")
        val descriptionInTable = if (trTags.size == 2) {
            val descriptionTr = trTags[1]
            if (descriptionTr.children().isNotEmpty()) {
                descriptionTr.child(0).text()
            } else {
                null
            }
        } else {
            null
        }
        val title = when {
            !spans.isEmpty() -> spans.html()
            !scpImageCaptions.isEmpty() -> scpImageCaptions.first()!!.html()
            descriptionInTable != null -> descriptionInTable
            else -> null
        }

        val imageTextPart = TextPart(data = null, type = TextType.IMAGE, orderInText = order)
        val imageUrlTextPart = TextPart(data = imageUrl, type = TextType.IMAGE_URL, orderInText = 0)
        val imageTitleTextPart = TextPart(data = title, type = TextType.IMAGE_TITLE, orderInText = 1)
        imageTextPart.innerTextParts = listOf(imageUrlTextPart, imageTitleTextPart)

        return imageTextPart
    }

    //todo print article in logs
    private fun parseSpoilerParts(html: String, order: Int): TextPart {
        val spoilerTextPart = TextPart(data = null, type = TextType.SPOILER, orderInText = order)

        val document = Jsoup.parse(html)

        //log.debug("document: $document")
        //parse collapsed part
        val elementFolded = document.getElementsByClass("collapsible-block-folded").first()!!
        val elementA = elementFolded.getElementsByTag(TAG_A).first()!!
        //replacing non-breaking-spaces
        val collapsedTitle = elementA.text().replace("\\p{Z}".toRegex(), " ")
        val collapsedSpoilerTextPart = TextPart(data = collapsedTitle, type = TextType.SPOILER_COLLAPSED, orderInText = 0)

        //parse expanded part
        val elementUnfolded = document.getElementsByClass("collapsible-block-unfolded").first()!!
        val elementUnfoldedBlockLink = elementUnfolded.getElementsByClass("collapsible-block-unfolded-link").first()
        val elementUnfoldedLink = elementUnfolded.getElementsByClass("collapsible-block-link").first()!!

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
                //in some articles there is really no data in spoiler...
                //so return empty string...
                //see `http://www.scp-wiki.net/sexycontainmentprocedures`
                if (elementUnfoldedContent.children().isEmpty()) {
                    log.debug("NO DATA IN SPOILER!")
                    spoilerData = ""
                } else {
//                log.debug("elementUnfoldedContent: $elementUnfoldedContent")
//                log.debug("elementUnfoldedContent: ${elementUnfoldedContent.parent()}")
//                log.debug("elementUnfoldedContent: ${elementUnfoldedContent.parent().parent()}")
                    throw ScpParseException("ERROR 0 WHILE PARSING SPOILER CONTENT. Please, let developers know about it, if you see this message)")
                }
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
                log.debug("NO DATA IN SPOILER!")
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
            val titlesTag = tabsElement.getElementsByClass("yui-nav").first()!!
            val liElements = titlesTag.getElementsByTag(TAG_LI)
            val tabsTitles = liElements.map { it.text() }

            val tabsTextPart = TextPart(data = null, type = TextType.TABS, orderInText = order)

            val yuiContent = tabsElement.getElementsByClass("yui-content").first()!!
            var tabOrder = 0

            //log.debug("Parse tabs: $yuiContent")
            tabsTextPart.innerTextParts = yuiContent.children().mapIndexed { index, element ->
                val tabTextPart = TextPart(data = tabsTitles[index], type = TextType.TAB, orderInText = tabOrder++)
                tabTextPart.innerTextParts = parseArticleText(element.html(), false)
                tabTextPart
            }

            return tabsTextPart
        } ?: throw ScpParseException("error parse tabs")
    }

    private fun parseList(html: String, order: Int, textType: TextType): TextPart {
        val listTextPart = TextPart(data = null, type = textType, orderInText = order)

        val document = Jsoup.parse(html)
        val tagToParse = when (textType) {
            TextType.NUMBERED_LIST -> TAG_OL
            TextType.BULLET_LIST -> TAG_UL
            else -> throw ScpParseException("Wrong textType while parse list tag: $textType", IllegalArgumentException())
        }
        val listTag = document.getElementsByTag(tagToParse).first()
                ?: throw ScpParseException("Error in list parsing. No list tag found", NullPointerException())

        val listItems = listTag
                .children()
                .filter { it.tagName() == TAG_LI }
                .mapIndexed { index, element -> parseListItem(element, index) }

        listTextPart.innerTextParts = listItems

        return listTextPart
    }

    private fun parseListItem(element: Element, order: Int): TextPart {
        val divTag = Element(TAG_SPAN)
        divTag.html(element.html())
        val listItem = TextPart(data = null, type = TextType.LIST_ITEM, orderInText = order)
        listItem.innerTextParts = parseArticleText(divTag.outerHtml(), false)
        return listItem
    }

    private fun getArticlesTextParts(html: String): List<String> {
        //log.debug("getArticlesTextParts: $html")
        val document = Jsoup.parse(html)
        val contentPage = document.getElementById(ID_PAGE_CONTENT) ?: document.body()
        return contentPage!!.children().map { it.outerHtml() }
    }

    private fun getListOfTextTypes(articlesTextParts: Iterable<String>): List<TextType> {
        val listOfTextTypes = mutableListOf<TextType>()
        for (textPart in articlesTextParts) {
            val element = Jsoup.parse(textPart)
            val ourElement = element.getElementsByTag(TAG_BODY).first()?.children()?.first()
            when {
                ourElement == null -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == TAG_P -> listOfTextTypes.add(TextType.TEXT)
                ourElement.tagName() == TAG_HR -> listOfTextTypes.add(TextType.HR)
                ourElement.tagName() == TAG_OL -> listOfTextTypes.add(TextType.NUMBERED_LIST)
                ourElement.tagName() == TAG_UL -> listOfTextTypes.add(TextType.BULLET_LIST)
                ourElement.tagName() == TAG_BLOCKQUOTE -> listOfTextTypes.add(TextType.BLOCKQUOTE)
                ourElement.className() == CLASS_SPOILER -> listOfTextTypes.add(TextType.SPOILER)
                ourElement.classNames().contains(CLASS_TABS) -> listOfTextTypes.add(TextType.TABS)
                isImageTextType(ourElement) -> listOfTextTypes.add(TextType.IMAGE)
                ourElement.tagName() == TAG_TABLE -> listOfTextTypes.add(TextType.TABLE)
                else -> listOfTextTypes.add(TextType.TEXT)
            }
        }

        return listOfTextTypes
    }

    private fun isImageTextType(element: Element): Boolean {
        //see `scp-1267`
        val isImageInTable = element.tagName() == TAG_TABLE
                && element.getElementsByTag("tr").size == 2
                && element.getElementsByTag("tr")[0].getElementsByTag(TAG_IMG).isNotEmpty()
        val isImageTextType = element.className() == "rimg"
                || element.className() == "limg"
                || element.className() == "cimg"
                || element.className() == "image"
                //see /operation-overmeta
                || (element.tagName() == TAG_A && element.children().size == 1 && element.children().first()?.tagName() == TAG_IMG)
                || element.classNames().contains("image-container")
                || element.classNames().contains("scp-image-block")

        return isImageTextType || isImageInTable
    }

    companion object {
        private const val TABLE_TEXT_COLOR_VAR_NAME = "TABLE_TEXT_COLOR_VAR_NAME"
        private const val TABLE_BACKGROUND_COLOR_VAR_NAME = "TABLE_BACKGROUND_COLOR_VAR_NAME"

        private fun createTableHtml(tableData: String): String {
            //update all links, as in new chrome we get `Not allowed to navigate top frame to data URL` error
            val doc = Jsoup.parse(tableData)
            doc.getElementsByTag(TAG_A).forEach {
                it.attr("onclick", """
                    (function() { 
                        JAVA_CODE.onLinkClicked('${it.attr(ATTR_HREF)}');
                        return false;
                    })();
                    return false; 
                """.trimIndent())
            }
            val updatedTableData = doc.body().html()
            return """
             |<!DOCTYPE html>
             |  <html>
             |      <head>
             |          <meta charset="utf-8">
             |          <meta name="viewport" content="width=device-width, user-scalable=yes" />
             |          <style>
             |              body{background-color: $TABLE_BACKGROUND_COLOR_VAR_NAME}
             |              table.wiki-content-table{border-collapse:collapse;border-spacing:0}
             |              table.wiki-content-table td{border:1px solid $TABLE_TEXT_COLOR_VAR_NAME;color: $TABLE_TEXT_COLOR_VAR_NAME;padding:.3em .7em;background-color: $TABLE_BACKGROUND_COLOR_VAR_NAME}
             |              table.wiki-content-table th{border:1px solid $TABLE_TEXT_COLOR_VAR_NAME;color: $TABLE_TEXT_COLOR_VAR_NAME;padding:.3em .7em;background-color: $TABLE_BACKGROUND_COLOR_VAR_NAME}
             |          </style>
             |      </head>
             |      <body>
             |          $updatedTableData
             |      </body>
             |  </html>
           """.trimMargin()
        }
    }
}

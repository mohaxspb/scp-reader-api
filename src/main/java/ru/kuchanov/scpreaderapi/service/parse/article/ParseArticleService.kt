package ru.kuchanov.scpreaderapi.service.parse.article

import org.apache.http.util.TextUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang
import ru.kuchanov.scpreaderapi.bean.articles.image.ArticlesImages
import ru.kuchanov.scpreaderapi.bean.articles.tags.TagForLang
import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_HREF
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.ATTR_SRC
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.NOT_TRANSLATED_ARTICLE_URL_DELIMITER
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.NOT_TRANSLATED_ARTICLE_UTIL_URL
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.SITE_TAGS_PATH
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_A
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_DIV
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_IFRAME
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_IMG
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_SPAN
import ru.kuchanov.scpreaderapi.service.parse.article.ParseConstants.TAG_TABLE
import java.util.*


@Service
class ParseArticleService @Autowired constructor(
        val parseArticleTextService: ParseArticleTextService
) {

    fun parseArticle(
            urlRelative: String,
            doc: Document,
            pageContent: Element,
            lang: Lang,
            printTextParts: Boolean = false
    ): ArticleForLang {
//        println("parseArticle: $urlRelative, ${lang.id}, $printTextParts")
        //some article are in div... I.e. http://scp-wiki-cn.wikidot.com/taboo
        //so check it and extract text
        if (pageContent.children().size == 1 && pageContent.children().first().tagName() == TAG_DIV) {
            val theOnlyChildDiv = pageContent.children().first()

            var child = theOnlyChildDiv.children().first()

            val children = mutableListOf<Node>()
            while (child != null) {
                children.add(child)
                //fixme check casting error in case for /scp-4000 on http://www.scp-wiki.net/scp-series-5
                child = child.nextSibling() as Element?
            }

            var prev: Node = theOnlyChildDiv
            for (node in children) {
                prev.after(node)
                prev = node
            }

            theOnlyChildDiv.remove()
        }


        //замена ссылок в сносках
        val footnoterefs = pageContent.getElementsByClass("footnoteref")
        for (snoska in footnoterefs) {
            val aTag = snoska.getElementsByTag(TAG_A).first()
            val digits = StringBuilder()
            for (c in aTag.id().toCharArray()) {
                if (c.isDigit()) {
                    digits.append(c.toString())
                }
            }
            aTag.attr(ATTR_HREF, "scp://$digits")
        }
        val footnoterefsFooter = pageContent.getElementsByClass("footnote-footer")
        for (snoska in footnoterefsFooter) {
            val aTag = snoska.getElementsByTag("a").first()
            snoska.prependText(aTag.text())
            aTag.remove()
            //                    aTag.replaceWith(new Element(Tag.valueOf("pizda"), aTag.text()));
        }

        //замена ссылок в библиографии
        val bibliographi = pageContent.getElementsByClass("bibcite")
        for (snoska in bibliographi) {
            val aTag = snoska.getElementsByTag("a").first()
            val onclickAttr = aTag.attr("onclick")

            val id = onclickAttr.substring(onclickAttr.indexOf("bibitem-"), onclickAttr.lastIndexOf("'"))
            aTag.attr(ATTR_HREF, id)
        }
        //remove rating bar
        var rating: Int? = null
        val rateDiv = pageContent.getElementsByClass("page-rate-widget-box").first()
        if (rateDiv != null) {
            val spanWithRating = rateDiv.getElementsByClass("rate-points").first()
            if (spanWithRating != null) {
                val ratingSpan = spanWithRating.getElementsByClass("number").first()
                if (ratingSpan != null && !TextUtils.isEmpty(ratingSpan.text())) {
                    try {
//                        rating = Integer.parseInt(ratingSpan.text().substring(1, ratingSpan.text().length))
                        rating = Integer.parseInt(ratingSpan.text())
                        //                            Timber.d("rating: %s", rating);
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            val span1 = rateDiv.getElementsByClass("rateup").first()
            span1.remove()
            val span2 = rateDiv.getElementsByClass("ratedown").first()
            span2.remove()
            val span3 = rateDiv.getElementsByClass("cancel").first()
            span3.remove()

            val heritageDiv = rateDiv.parent().getElementsByClass("heritage-emblem")
            if (heritageDiv != null && !heritageDiv.isEmpty()) {
                heritageDiv.first().remove()
            }

            //attempt to remove parent div, if it has only this child
            if (rateDiv.parent().childNodeSize() == 1) {
                rateDiv.parent().remove()
            } else {
                rateDiv.remove()
            }
        }
        //remove something more
        val svernut = pageContent.getElementById("toc-action-bar")
        svernut?.remove()
        val script = pageContent.getElementsByTag("script")
        for (element in script) {
            element.remove()
        }
        //remove bottom navigation bar
        val bottomNavigationBar = pageContent.getElementsByClass("footer-wikiwalk-nav")
        if (bottomNavigationBar.isNotEmpty()) {
            bottomNavigationBar.first().remove()
        }
        //remove audio link from DE version
        val audio = pageContent.getElementsByClass("audio-img-block")
        audio?.remove()
        val audioContent = pageContent.getElementsByClass("audio-block")
        audioContent?.remove()
        val creditRate = pageContent.getElementsByClass("creditRate")
        creditRate?.remove()

        val uCreditView = pageContent.getElementById("u-credit-view")
        uCreditView?.remove()
        val uCreditOtherwise = pageContent.getElementById("u-credit-otherwise")
        uCreditOtherwise?.remove()
        //remove audio link from DE version END

        //replace all spans with strike-through with <s>
        val spansWithStrike = pageContent.select("span[style=text-decoration: line-through;]")
        for (element in spansWithStrike) {
            //                    Timber.d("element: %s", element);
            element.tagName("s")
            for (attribute in element.attributes()) {
                element.removeAttr(attribute.key)
            }
            //                    Timber.d("element refactored: %s", element);
        }

        //some fucking articles have all its content in 2 div... WTF?! One more fucking Kludge.
        //see http://scpfoundation.net/scp-2111/offset/2
        val divWithAllContent = pageContent.getElementsByClass("list-pages-box").first()
        if (divWithAllContent != null) {
            val innerDiv = divWithAllContent.getElementsByClass("list-pages-item").first()
            if (innerDiv != null) {
                var prevElement: Element = divWithAllContent
                for (contentElement in innerDiv.children()) {
                    prevElement.after(contentElement)
                    prevElement = contentElement
                }
                divWithAllContent.remove()
            }
        }

        //get title
        val titleEl = doc.getElementById("page-title")
        var title = ""
        if (titleEl != null) {
            title = titleEl.text()
        } else if (urlRelative.contains(SITE_TAGS_PATH)) {
            val decodedUrl = java.net.URLDecoder.decode(urlRelative, "UTF-8")
            val tagName = decodedUrl.substring(urlRelative.lastIndexOf(SITE_TAGS_PATH) + SITE_TAGS_PATH.length)
            title = "TAG: $tagName"
        }
        val upperDivWithLink = doc.getElementById("breadcrumbs")
        if (upperDivWithLink != null) {
            pageContent.prependChild(upperDivWithLink)
        }
        parseImgsTags(pageContent)

        //extract tables, which are single tag in div
        extractTablesFromDivs(pageContent)
        //unwrap divs with text alignment
        unwrapTextAlignmentDivs(pageContent)

        //put all text which is not in any tag in div tag
        for (element in pageContent.children()) {
            val nextSibling = element.nextSibling()
            if (nextSibling != null && nextSibling.toString() != " " && nextSibling.nodeName() == "#text") {
                element.after(Element(TAG_DIV).appendChild(nextSibling))
            }

            //also fix scp-3000, where image and spoiler are in div tag, fucking shit! Web monkeys, ARGH!!!
            if (!element.children().isEmpty()
                    && element.children().size == 2
                    && element.child(0).tagName() == TAG_IMG
                    && element.child(1).className() == "collapsible-block") {
                val imgTag = element.childNode(0)
                val spoiler = element.childNode(1)
                element.before(imgTag)
                element.after(spoiler)
                element.remove()
            }
        }

        //replace styles with underline and strike
        val spans = pageContent.getElementsByTag(TAG_SPAN)
        for (element in spans) {
            //<span style="text-decoration: underline;">PLEASE</span>
            if (element.hasAttr("style") && element.attr("style") == "text-decoration: underline;") {
                val uTag = Element(Tag.valueOf("u"), "").text(element.text())
                element.replaceWith(uTag)
            }
            //<span style="text-decoration: line-through;">условия содержания.</span>
            if (element.hasAttr("style") && element.attr("style") == "text-decoration: line-through;") {
                val sTag = Element(Tag.valueOf("s"), "")
                element.replaceWith(sTag)
            }
        }

        //search for relative urls to add domain
        for (a in pageContent.getElementsByTag(TAG_A)) {
            //replace all links to not translated articles
            if (a.className() == "newpage") {
                a.attr(ATTR_HREF, NOT_TRANSLATED_ARTICLE_UTIL_URL
                        + NOT_TRANSLATED_ARTICLE_URL_DELIMITER
                        + a.attr(ATTR_HREF)
                )
            } else if (a.attr(ATTR_HREF).startsWith("/")) {
                a.attr(ATTR_HREF, lang.siteBaseUrl + a.attr(ATTR_HREF))
            }
        }

        //extract tags
        val articleTags = mutableListOf<String>()
        val tagsContainer = doc.getElementsByClass("page-tags").first()
        if (tagsContainer != null) {
            for (a in tagsContainer.getElementsByTag(TAG_A)) {
                articleTags.add(a.text())
            }
        }

        //search for images and add it to separate field to be able to show it in arts lists
        val imgsUrls = mutableListOf<String>()
        val imgsOfArticle = pageContent.getElementsByTag(TAG_IMG)
        if (!imgsOfArticle.isEmpty()) {
            for (img in imgsOfArticle) {
                imgsUrls.add(img.attr(ATTR_SRC))
            }
        }

        //search for inner articles
        val innerArticlesUrls = mutableListOf<String>()
        val innerATags = pageContent.getElementsByTag(TAG_A)
        if (!innerATags.isEmpty()) {
            for (a in innerATags) {
                val innerUrl = a.attr(ATTR_HREF)
                if (LinkType.getLinkType(innerUrl, lang) === LinkType.INNER) {
                    innerArticlesUrls.add(LinkType.getFormattedUrl(innerUrl, lang))
                }
            }
        }

        //this we store as article text
        val rawText = pageContent.toString()

        //do not parse textParts if article has iframe
        val hasIframeTag = pageContent.getElementsByTag(TAG_IFRAME).isNotEmpty()
        val textParts = if (hasIframeTag) {
            println("IFRAME FOUND for article: $urlRelative for lang: ${lang.id}!")
            emptyList()
        } else {
            parseArticleTextService.parseArticleText(rawText, printTextParts)
        }

        if (printTextParts) {
            println("===================== parsed text parts =====================")
            textParts.forEach {
                println(it)
                println("\n")
            }
            println("===========END========== parsed text parts ===========END==========")
        }

        //comments url
        val commentsUrl = doc.getElementById("discuss-button")?.attr("href")?.let {
            "${lang.siteBaseUrl}$it"
        }

//        println("parseArticle: $urlRelative, ${lang.id}, $printTextParts END")

        //finally fill article info
        return ArticleForLang(
                langId = lang.id,
                urlRelative = urlRelative,
                title = title,
                text = rawText,
                rating = rating,
                commentsUrl = commentsUrl,
                hasIframeTag = hasIframeTag,
                images = imgsUrls.map { ArticlesImages(url = it) }.toMutableSet(),
                tags = articleTags.map { TagForLang(langId = lang.id, title = it) }.toMutableSet(),
                textParts = textParts,
                innerArticlesForLang = innerArticlesUrls.map {
                    ArticleForLang(
                            langId = lang.id,
                            urlRelative = it.replace(lang.siteBaseUrl, "").trim(),
                            title = ""
                    )
                }.toSet()
        )
    }

    /**
     * formats HTML img tags to common format
     */
    private fun parseImgsTags(pageContent: Element) {
        parseRimgLimgCimgImages("rimg", pageContent)
        parseRimgLimgCimgImages("limg", pageContent)
        parseRimgLimgCimgImages("cimg", pageContent)
    }

    private fun parseRimgLimgCimgImages(className: String, pageContent: Element) {
        //parse multiple imgs in "rimg" tag
        val rimgs = pageContent.getElementsByClass(className)
        if (rimgs != null) {
            for (rimg in rimgs) {
                val imgs = rimg.getElementsByTag(TAG_IMG)

                if (imgs != null && imgs.size > 1) {
                    val rimgsToAdd = ArrayList<Element>()
                    for (i in 0 until imgs.size) {
                        val img = imgs[i]

                        var nextImgSibling: Element? = img.nextElementSibling()

                        val descriptions = Elements()
                        while (nextImgSibling != null && nextImgSibling.tagName() != TAG_IMG) {
                            descriptions.add(nextImgSibling)
                            nextImgSibling = nextImgSibling.nextElementSibling()
                        }

                        val newRimg = Element(TAG_DIV)
                        newRimg.addClass(className)
                        newRimg.appendChild(img)

                        for (element in descriptions) {
                            newRimg.appendChild(element)
                        }

                        rimgsToAdd.add(newRimg)
                    }
                    var rimgLast = rimg
                    for (newRimg in rimgsToAdd) {
                        rimgLast.after(newRimg)
                        rimgLast = newRimg
                    }
                    rimg.remove()
                }
            }
        }
    }

    private fun extractTablesFromDivs(pageContent: Element) {
        for (ourElement in pageContent.getElementsByTag(TAG_DIV)) {
            if (ourElement.children().size == 1
                    && ourElement.child(0).tagName() == TAG_TABLE
                    && !ourElement.hasClass("collapsible-block-content")) {
                ourElement.appendChild(ourElement.child(0))
                ourElement.remove()
            }
        }
    }

    private fun unwrapTextAlignmentDivs(element: Element) {
        val children = element.children()
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
}

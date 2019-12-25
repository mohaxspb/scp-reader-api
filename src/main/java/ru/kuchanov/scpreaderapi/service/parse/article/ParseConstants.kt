package ru.kuchanov.scpreaderapi.service.parse.article

import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.utils.isDigitsOnly

object ParseConstants {

    const val TAG_IMG = "img"

    const val TAG_SPAN = "span"

    const val TAG_DIV = "div"

    const val TAG_A = "a"

    const val TAG_P = "p"

    const val TAG_BLOCKQUOTE = "blockquote"

    const val TAG_BODY = "body"

    const val TAG_TABLE = "table"

    const val TAG_LI = "li"

    const val TAG_IFRAME = "iframe"

    const val ATTR_SRC = "src"

    const val ATTR_HREF = "href"

    const val ID_PAGE_CONTENT = "page-content"

    const val CLASS_TABS = "yui-navset"

    const val CLASS_SPOILER = "collapsible-block"

    //misc
    const val SITE_TAGS_PATH = "system:page-tags/tag/"

    const val NOT_TRANSLATED_ARTICLE_UTIL_URL = "not_translated_yet_article_which_we_cant_show"

    const val NOT_TRANSLATED_ARTICLE_URL_DELIMITER = "___"
}

enum class LinkType {

    JAVASCRIPT, SNOSKA, BIBLIOGRAPHY, TOC, MUSIC, NOT_TRANSLATED, EXTERNAL, INNER;

    companion object {

        fun getLinkType(link: String, lang: Lang): LinkType {
            if (link.contains("javascript")) {
                return JAVASCRIPT
            }
            if (link.isDigitsOnly() || link.startsWith("scp://")) {
                return SNOSKA
            }
            if (link.startsWith("bibitem-")) {
                return BIBLIOGRAPHY
            }
            if (link.startsWith("#")) {
                return TOC
            }
            if (link.endsWith(".mp3")) {
                return MUSIC
            }
            if (link.startsWith(ParseConstants.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                return NOT_TRANSLATED
            }
            return if (!link.startsWith(lang.siteBaseUrl) || link.startsWith(lang.siteBaseUrl + "/forum")) {
                EXTERNAL
            } else INNER
        }

        fun getFormattedUrl(url: String, lang: Lang) =
                when (getLinkType(url, lang)) {
                    JAVASCRIPT, INNER, TOC, MUSIC, EXTERNAL, BIBLIOGRAPHY -> {
                        if (!url.startsWith("http") && !url.startsWith(ParseConstants.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                            lang.siteBaseUrl + url
                        } else {
                            url
                        }
                    }
                    SNOSKA -> {
                        if (url.startsWith("scp://")) {
                            url.replace("scp://", "")
                        } else {
                            url
                        }
                    }
                    NOT_TRANSLATED -> {
                        if (url.startsWith(ParseConstants.NOT_TRANSLATED_ARTICLE_UTIL_URL)) {
                            url.split(ParseConstants.NOT_TRANSLATED_ARTICLE_URL_DELIMITER)[1]
                        } else {
                            url
                        }
                    }
                }
    }
}

enum class TextType {
    TEXT,
    SPOILER,
    SPOILER_EXPANDED,
    SPOILER_COLLAPSED,
    IMAGE,
    IMAGE_URL,
    IMAGE_TITLE,
    TABLE,
    TABS,
    TAB,
    BLOCKQUOTE
}

package ru.kuchanov.scpreaderapi.service.parse.article

import ru.kuchanov.scpreaderapi.bean.users.Lang
import ru.kuchanov.scpreaderapi.utils.isDigitsOnly

object ParseConstants {

    const val TAG_IMG = "img"

    const val TAG_SPAN = "span"

    const val TAG_DIV = "div"

    const val TAG_A = "a"

    const val TAG_P = "p"

    const val TAG_HR = "hr"

    const val TAG_BLOCKQUOTE = "blockquote"

    const val TAG_BODY = "body"

    const val TAG_TABLE = "table"

    const val TAG_UL = "ul"

    const val TAG_OL = "ol"

    const val TAG_LI = "li"

    const val TAG_IFRAME = "iframe"

    const val ATTR_SRC = "src"

    const val ATTR_HREF = "href"

    const val ID_PAGE_CONTENT = "page-content"

    const val CLASS_TABS = "yui-navset"

    const val CLASS_SPOILER = "collapsible-block"

    //misc
    const val SITE_TAGS_PATH = "system:page-tags/tag/"

    const val NOT_TRANSLATED_ARTICLE_UTIL_URL = "NOT_TRANSLATED_ARTICLE_UTIL_URL"

    const val NOT_TRANSLATED_ARTICLE_URL_DELIMITER = "___"
}

enum class LinkType {

    JAVASCRIPT, SNOSKA, BIBLIOGRAPHY, TOC, MUSIC, NOT_TRANSLATED, EXTERNAL, INNER;

    companion object {

        fun getLinkType(link: String, lang: Lang): LinkType =
                when {
                    link.contains("javascript") -> JAVASCRIPT
                    link.isDigitsOnly() || link.startsWith("scp://") -> SNOSKA
                    link.startsWith("bibitem-") -> BIBLIOGRAPHY
                    link.startsWith("#") -> TOC
                    link.endsWith(".mp3") -> MUSIC
                    link.startsWith(ParseConstants.NOT_TRANSLATED_ARTICLE_UTIL_URL) -> NOT_TRANSLATED
                    isExternalLink(link, lang) -> EXTERNAL
                    else -> INNER
                }

        private fun isExternalLink(link: String, lang: Lang): Boolean {
            if (link.startsWith("/forum")) {
                return true
            } else {
                lang.siteBaseUrlsToLangs?.forEach {
                    if (link.startsWith(it.siteBaseUrl)) {
                        return false
                    }
                }
                if (link.startsWith("http")) {
                    return true
                }
                return false
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
    BLOCKQUOTE,
    HR,
    NUMBERED_LIST,
    BULLET_LIST,
    LIST_ITEM
}

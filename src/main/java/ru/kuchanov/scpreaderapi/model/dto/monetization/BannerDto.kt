package ru.kuchanov.scpreaderapi.model.dto.monetization

import ru.kuchanov.scpreaderapi.bean.ads.Banner


data class BannerDto(
        val title: String,
        val subTitle: String,
        val ctaButtonText: String,
        val redirectUrl: String,

        val enabled: Boolean
)

fun BannerDto.toBanner() = Banner(
        title = title,
        subTitle = subTitle,
        ctaButtonText = ctaButtonText,
        redirectUrl = redirectUrl,
        enabled = enabled
)
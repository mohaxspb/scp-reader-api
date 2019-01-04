package ru.kuchanov.scpreaderapi.model.dto.monetization

import ru.kuchanov.scpreaderapi.bean.ads.Banner
import ru.kuchanov.scpreaderapi.bean.ads.BannerType


data class BannerDto(
        val title: String,
        val subTitle: String,
        val ctaButtonText: String,
        val redirectUrl: String,
        val bannerType: BannerType,
        val enabled: Boolean
)

fun BannerDto.toBanner() = Banner(
        title = title,
        subTitle = subTitle,
        ctaButtonText = ctaButtonText,
        redirectUrl = redirectUrl,
        bannerType = bannerType,
        enabled = enabled
)
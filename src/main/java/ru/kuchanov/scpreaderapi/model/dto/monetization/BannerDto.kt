package ru.kuchanov.scpreaderapi.model.dto.monetization


data class BannerDto(
        val title: String,
        val subTitle: String,
        val ctaButtonText: String,
        val redirectUrl: String,

        val enabled: Boolean
)
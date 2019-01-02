package ru.kuchanov.scpreaderapi.service.monetization.ads

import ru.kuchanov.scpreaderapi.bean.ads.Banner

interface BannersService {

    fun getById(id: Long): Banner?

    fun findAll(): List<Banner>

    fun save(banner: Banner): Banner
    fun saveAll(banners: List<Banner>): List<Banner>

    fun deleteById(id: Long)
}
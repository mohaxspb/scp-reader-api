package ru.kuchanov.scpreaderapi.service.monetization.ads

import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.scpreaderapi.bean.ads.Banner

interface BannersService {

    fun getById(id: Long): Banner?

    fun findAll(): List<Banner>

    fun save(banner: Banner): Banner
    fun saveAll(banners: List<Banner>): List<Banner>

    fun deleteById(id: Long)

    fun saveFile(image: MultipartFile, id: Long, name: String): String

    fun deleteFilesById(id: Long)
}
package ru.kuchanov.scpreaderapi.repository.gallery

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImage

interface GalleryRepository : JpaRepository<GalleryImage, Long> {
    fun getOneByVkId(vkId: Long): GalleryImage?
}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such gallery image")
class GalleryImageNotFoundException : RuntimeException()
package ru.kuchanov.scpreaderapi.repository.gallery

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImageTranslation

interface GalleryTranslationRepository : JpaRepository<GalleryImageTranslation, Long> {
    fun getOneByTranslation(translation: String): GalleryImageTranslation?
}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such gallery image translation")
class GalleryImageTranslationNotFoundException : RuntimeException()
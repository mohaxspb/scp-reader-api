package ru.kuchanov.scpreaderapi.service.gallery

import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImageTranslation

interface GalleryTranslationService {
    fun findAll(): List<GalleryImageTranslation>
    fun getById(id: Long): GalleryImageTranslation
    fun getOneByTranslation(translation: String): GalleryImageTranslation?
    fun update(galleryImage: GalleryImageTranslation): GalleryImageTranslation
    fun saveAll(images: List<GalleryImageTranslation>): List<GalleryImageTranslation>
    fun save(galleryImageTranslation: GalleryImageTranslation): GalleryImageTranslation?
}
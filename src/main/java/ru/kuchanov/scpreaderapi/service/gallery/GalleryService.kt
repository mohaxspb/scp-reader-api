package ru.kuchanov.scpreaderapi.service.gallery

import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImage

interface GalleryService {
    fun findAll(): List<GalleryImage>
    fun getById(id: Long): GalleryImage
    fun getByVkId(vkId: Long): GalleryImage?
    fun update(galleryImage: GalleryImage): GalleryImage
    fun saveAll(images: List<GalleryImage>): List<GalleryImage>
    fun save(galleryImage: GalleryImage): GalleryImage?
    fun deleteById(id: Long)
}
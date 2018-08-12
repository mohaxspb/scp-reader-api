package ru.kuchanov.scpreaderapi.service.gallery

import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImage

interface GalleryService {
    fun findAll(): List<GalleryImage>
    fun getById(id: Long): GalleryImage
    fun update(galleryImage: GalleryImage): GalleryImage
    fun saveAll(images: List<GalleryImage>): List<GalleryImage>
}
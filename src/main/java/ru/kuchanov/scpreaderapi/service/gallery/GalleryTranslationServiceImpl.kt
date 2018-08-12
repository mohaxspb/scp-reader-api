package ru.kuchanov.scpreaderapi.service.gallery

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImageTranslation
import ru.kuchanov.scpreaderapi.repository.gallery.GalleryImageNotFoundException
import ru.kuchanov.scpreaderapi.repository.gallery.GalleryTranslationRepository


@Service
class GalleryTranslationServiceImpl : GalleryTranslationService {

    @Autowired
    private lateinit var repository: GalleryTranslationRepository

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: Long) = repository.getOne(id) ?: throw GalleryImageNotFoundException()

    override fun getOneByTranslation(translation: String): GalleryImageTranslation? = repository.getOneByTranslation(translation)
    override fun update(galleryImage: GalleryImageTranslation): GalleryImageTranslation = repository.save(galleryImage)

    override fun save(galleryImageTranslation: GalleryImageTranslation): GalleryImageTranslation = repository.save(galleryImageTranslation)
    override fun saveAll(images: List<GalleryImageTranslation>): MutableList<GalleryImageTranslation> = repository.saveAll(images)
}
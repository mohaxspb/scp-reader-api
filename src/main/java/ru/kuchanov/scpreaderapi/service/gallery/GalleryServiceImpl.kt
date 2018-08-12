package ru.kuchanov.scpreaderapi.service.gallery

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImage
import ru.kuchanov.scpreaderapi.repository.gallery.GalleryImageNotFoundException
import ru.kuchanov.scpreaderapi.repository.gallery.GalleryRepository


@Service
class GalleryServiceImpl : GalleryService {

    @Autowired
    private lateinit var repository: GalleryRepository

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: Long) = repository.getOne(id) ?: throw GalleryImageNotFoundException()

    override fun getByVkId(vkId: Long): GalleryImage? = repository.getOneByVkId(vkId)

    override fun update(galleryImage: GalleryImage): GalleryImage = repository.save(galleryImage)

    override fun save(galleryImage: GalleryImage): GalleryImage = repository.save(galleryImage)

    override fun saveAll(images: List<GalleryImage>): MutableList<GalleryImage> = repository.saveAll(images)

    override fun deleteById(id: Long) = repository.deleteById(id)
}
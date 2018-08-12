package ru.kuchanov.scpreaderapi.network

import com.vk.api.sdk.objects.photos.Photo
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImage
import ru.kuchanov.scpreaderapi.bean.gallery.GalleryImageTranslation

class ModelConverter {

    fun convert(photosFromVk: List<Photo>) = photosFromVk.map {
        GalleryImage(
                imageUrl = it.sizes.last().src,
                galleryImageTranslations = mutableSetOf(GalleryImageTranslation(
                        langCode = Constants.LangCodes.RU,
                        translation = it.text,
                        authorId = 1
                )),
                authorId = 1,
                vkId = it.id.toLong()
        )
    }
}
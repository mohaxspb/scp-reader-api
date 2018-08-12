package ru.kuchanov.scpreaderapi.controller

import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import ru.kuchanov.scpreaderapi.Constants
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.network.ModelConverter
import ru.kuchanov.scpreaderapi.service.gallery.GalleryService
import ru.kuchanov.scpreaderapi.service.gallery.GalleryTranslationService
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths


@RestController
@RequestMapping("/${Constants.GALLERY_PATH}")
class GalleryController {

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var apiClient: ApiClient

    @Autowired
    private lateinit var modelConverter: ModelConverter

    @Autowired
    lateinit var galleryService: GalleryService

    @Autowired
    lateinit var galleryTranslationService: GalleryTranslationService

    @Scheduled(
            fixedDelay = Constants.GALLERY_UPDATE_FROM_VK_RATE_MILLIS,
            initialDelay = Constants.GALLERY_UPDATE_FROM_VK_RATE_MILLIS
    )
    @GetMapping("/updateFromVk")
    fun updateFromVk(): ResponseEntity<String> {
        apiClient.getScpArtPhotosFromVk()?.let { getResponse ->
            val galleryPhotosFromVk = modelConverter.convert(getResponse.items)

            var newImagesSavedCount = 0
            var updatedImagesCount = 0

            galleryPhotosFromVk.forEach {
                var galleryImageInDb = galleryService.getByVkId(it.vkId)
                if (galleryImageInDb != null) {
                    val galleryImageTranslationFromVk = it.galleryImageTranslations.first()
                    val galleryImageTranslationInDb = galleryTranslationService.getOneByTranslation(
                            galleryImageTranslationFromVk.translation
                    )
                    if (galleryImageTranslationInDb != null) {
                        galleryImageTranslationInDb.translation = galleryImageTranslationFromVk.translation
                        galleryTranslationService.update(galleryImageTranslationInDb)
                    } else {
                        galleryTranslationService.save(galleryImageTranslationFromVk)
                    }
                    updatedImagesCount++
                } else {
                    galleryImageInDb = galleryService.save(it)
                    newImagesSavedCount++
                }
                downloadImageFromUrl(it.imageUrl, galleryImageInDb!!.id!!)
            }

            return ResponseEntity.ok().body("{status:\"saved: $newImagesSavedCount, updated: $updatedImagesCount\"}")
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{status:\"error\"}")
    }

    @GetMapping("/all")
    fun getGallery() = galleryService.findAll()

    @GetMapping("/{id}/delete")
    fun deleteGalleryImageById(@PathVariable(value = "id") id: Long) = galleryService.deleteById(id)

    @ResponseBody
    @GetMapping(value = ["/files/{id}"], produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getGalleryImageFileById(@PathVariable(value = "id") id: Long): ByteArray {
        val fileName = "gallery/$id.jpg"
        if (Files.exists(Paths.get(fileName))) {
            val inputStream = FileInputStream("gallery/$id.jpg")
            return IOUtils.toByteArray(inputStream)
        } else {
            throw MyFileNotFoundException("File not found $fileName", IOException())
        }
    }

    @GetMapping("/files/extensions/all")
    fun getFilesExtensions(): Any {
        val fileIdsWithNotJpg = mutableListOf<Long>()
        val fileIdsWithJpg = mutableListOf<Long>()
        galleryService.findAll().forEach {
            if (it.imageUrl.substringAfterLast(".") != "jpg") {
                fileIdsWithNotJpg.add(it.id!!)
            } else {
                fileIdsWithJpg.add(it.id!!)
            }
        }

        data class Response(
                val fileIdsWithNotJpg: List<Long>,
                val fileIdsWithJpg: List<Long>
        )

        return Response(fileIdsWithNotJpg, fileIdsWithJpg)
    }

    private fun downloadImageFromUrl(url: String, id: Long) {
        val readableByteChannel = Channels.newChannel(URL(url).openStream())
        Files.createDirectories(Paths.get("gallery"))
        val fileOutputStream = FileOutputStream("gallery/$id.jpg")
        fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class MyFileNotFoundException(message: String, exception: Exception) : RuntimeException()
}
package ru.kuchanov.scpreaderapi.controller.monetization

import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.ads.Banner
import ru.kuchanov.scpreaderapi.bean.ads.BannerNotFoundException
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.controller.GalleryController
import ru.kuchanov.scpreaderapi.model.dto.monetization.BannerDto
import ru.kuchanov.scpreaderapi.model.dto.monetization.toBanner
import ru.kuchanov.scpreaderapi.service.monetization.ads.BannersService
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


@RestController
@RequestMapping("/${ScpReaderConstants.Path.ADS}")
class AdsController {

    @Autowired
    private lateinit var bannersService: BannersService

    //todo check auth and return only owned
    @GetMapping("/all")
    fun getAll() = bannersService.findAll()

    @PreAuthorize("hasAuthority('BANNER')")
    @PostMapping("/create")
    fun addBanner(
            @RequestParam("image") image: MultipartFile,
            @RequestParam("logo") logo: MultipartFile,
            @ModelAttribute bannerDto: BannerDto,
            @AuthenticationPrincipal user: User
    ): Banner {
        println("image: ${image.originalFilename}")
        println("logo: ${logo.originalFilename}")
        println("bannerDto: $bannerDto")

        val banner = bannersService.save(bannerDto.toBanner().apply { authorId = user.id })

        banner.id?.let {
            banner.imageUrl = bannersService.saveFile(image, it, "image")
            banner.logoUrl = bannersService.saveFile(logo, it, "logo")

            return bannersService.save(banner)
        } ?: throw IllegalStateException("Error while create banner", NullPointerException())
    }

    @ResponseBody
    @GetMapping(
            value = ["/{id}/{name}"],
            produces = [
                MediaType.IMAGE_JPEG_VALUE,
                MediaType.IMAGE_PNG_VALUE,
                MediaType.IMAGE_GIF_VALUE
            ]
    )
    fun getGalleryImageFileById(
            @PathVariable(value = "id") id: Long,
            @PathVariable(value = "name") name: String
    ): ByteArray {
        val fileName = "${ScpReaderConstants.FilesPaths.BANNERS}/$id/$name"
        if (Files.exists(Paths.get(fileName))) {
            val inputStream = FileInputStream(fileName)
            return IOUtils.toByteArray(inputStream)
        } else {
            throw GalleryController.MyFileNotFoundException("File not found $fileName", IOException())
        }
    }

    @PreAuthorize("hasAuthority('BANNER')")
    @PostMapping("/{id}/enable")
    fun enableBanner(
            @PathVariable(value = "id") id: Long,
            @RequestParam("enable") enable: Boolean,
            @AuthenticationPrincipal user: User
    ): Banner {
        val banner = bannersService.getById(id) ?: throw BannerNotFoundException()

        banner.enabled = enable

        return bannersService.save(banner)
    }

    @PreAuthorize("hasAuthority('BANNER')")
    @DeleteMapping("/delete/{id}")
    fun deleteBanner(
            @PathVariable(value = "id") id: Long,
            @AuthenticationPrincipal user: User
    ): Banner {
        val banner = bannersService.getById(id) ?: throw BannerNotFoundException()

        banner.id?.let {
            bannersService.deleteById(it)
            bannersService.deleteFilesById(it)
        }

        return banner
    }
}
package ru.kuchanov.scpreaderapi.controller.monetization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.model.dto.monetization.BannerDto
import ru.kuchanov.scpreaderapi.service.monetization.ads.BannersService


@RestController
@RequestMapping("/${ScpReaderConstants.Path.ADS}")
class AdsController {

    @Autowired
    private lateinit var bannersService: BannersService

    @GetMapping("/all")
    fun getAll() = bannersService.findAll()

    @PostMapping("/create")
    fun addBanner(
            @RequestParam("image") image: MultipartFile,
            @RequestParam("logo") logo: MultipartFile,
            @ModelAttribute banner: BannerDto
    ) {
        println("image: ${image.originalFilename}")
        println("logo: ${logo.originalFilename}")

        println("banner: $banner")
        //todo
    }
}
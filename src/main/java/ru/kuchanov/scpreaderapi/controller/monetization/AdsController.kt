package ru.kuchanov.scpreaderapi.controller.monetization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.service.monetization.ads.BannersService


@RestController
@RequestMapping("/${ScpReaderConstants.Path.ADS}")
class AdsController {

    @Autowired
    private lateinit var bannersService: BannersService

    @GetMapping("/all")
    fun getAll() = bannersService.findAll()
}
package ru.kuchanov.scpreaderapi.service.monetization.ads

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.ads.Banner
import ru.kuchanov.scpreaderapi.repository.monetization.ads.BannersRepository


@Service
class BannersServiceImpl : BannersService {

    @Autowired
    private lateinit var repository: BannersRepository

    override fun getById(id: Long): Banner? = repository.getOneById(id)

    override fun findAll(): List<Banner> = repository.findAll()

    override fun save(banner: Banner): Banner = repository.save(banner)
    override fun saveAll(banners: List<Banner>): List<Banner> = repository.saveAll(banners)

    override fun deleteById(id: Long) = repository.deleteById(id)
}
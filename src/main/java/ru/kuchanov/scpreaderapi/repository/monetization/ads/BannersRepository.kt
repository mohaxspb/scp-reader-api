package ru.kuchanov.scpreaderapi.repository.monetization.ads

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.ads.Banner

interface BannersRepository : JpaRepository<Banner, Long> {
    fun getOneById(id: Long): Banner?
}

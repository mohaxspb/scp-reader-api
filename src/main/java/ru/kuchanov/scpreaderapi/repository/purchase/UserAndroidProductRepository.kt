package ru.kuchanov.scpreaderapi.repository.purchase

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct

interface UserAndroidProductRepository : JpaRepository<UsersAndroidProduct, Long>
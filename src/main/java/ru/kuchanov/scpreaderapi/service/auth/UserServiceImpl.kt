package ru.kuchanov.scpreaderapi.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.auth.User
import ru.kuchanov.scpreaderapi.bean.auth.UserNotFoundException
import ru.kuchanov.scpreaderapi.repository.auth.UsersRepository


@Service
class UserServiceImpl : UserService {

    @Autowired
    private lateinit var repository: UsersRepository

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: Long) = repository.getOne(id) ?: throw UserNotFoundException()

    override fun update(user: User): User = repository.save(user)

    override fun loadUserByUsername(username: String) = repository.findOneByMyUsername(username)
}
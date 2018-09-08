package ru.kuchanov.scpreaderapi.service.users

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.bean.users.User
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.repository.users.UsersRepository


@Service
class UserServiceImpl : UserService {

    @Autowired
    private lateinit var repository: UsersRepository

    override fun findAll() = repository.findAll().toList()

    override fun getById(id: Long) = repository.getOne(id) ?: throw UserNotFoundException()

    override fun getByUsername(username: String) = repository.findOneByMyUsername(username)

    override fun update(user: User): User = repository.save(user)

    override fun loadUserByUsername(username: String) = repository.findOneByMyUsername(username)

    override fun insert(user: User): User = repository.save(user)

    override fun insert(users: List<User>): List<User> = repository.saveAll(users)
}
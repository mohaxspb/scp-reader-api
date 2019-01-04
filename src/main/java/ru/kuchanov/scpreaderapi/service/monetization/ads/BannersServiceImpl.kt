package ru.kuchanov.scpreaderapi.service.monetization.ads

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.ads.Banner
import ru.kuchanov.scpreaderapi.repository.monetization.ads.BannersRepository
import java.io.FileOutputStream
import java.nio.channels.Channels
import java.nio.file.Files
import java.nio.file.Paths


@Service
class BannersServiceImpl : BannersService {

    @Autowired
    private lateinit var repository: BannersRepository

    override fun getById(id: Long): Banner? = repository.getOneById(id)

    override fun findAll(): List<Banner> = repository.findAll()

    override fun findAllByAuthorId(authorId: Long): List<Banner> =
            repository.findAllByAuthorId(authorId)

    override fun save(banner: Banner): Banner = repository.save(banner)
    override fun saveAll(banners: List<Banner>): List<Banner> = repository.saveAll(banners)

    override fun deleteById(id: Long) = repository.deleteById(id)

    override fun saveFile(image: MultipartFile, id: Long, name: String): String {
        val fileDir = "${ScpReaderConstants.FilesPaths.BANNERS}/$id"
        val fileName = "$fileDir/$name"

        val readableByteChannel = Channels.newChannel(image.inputStream)
        Files.createDirectories(Paths.get(fileDir))
        val fileOutputStream = FileOutputStream(fileName)
        fileOutputStream.channel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)

        return "${ScpReaderConstants.Path.ADS}/$id/$name"
    }

    override fun deleteFilesById(id: Long) {
        val fileDir = "${ScpReaderConstants.FilesPaths.BANNERS}/$id"
        FileSystemUtils.deleteRecursively(Paths.get(fileDir))
    }
}
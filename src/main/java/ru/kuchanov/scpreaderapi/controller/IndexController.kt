package ru.kuchanov.scpreaderapi.controller

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.Constants.BACK_UP_RATE_MILLIS
import ru.kuchanov.scpreaderapi.bean.auth.User
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.network.ModelConverter
import ru.kuchanov.scpreaderapi.service.auth.AuthorityService
import ru.kuchanov.scpreaderapi.service.auth.UserService
import ru.kuchanov.scpreaderapi.service.gallery.GalleryService
import java.io.File
import java.io.FilenameFilter
import java.text.SimpleDateFormat
import java.util.*


@RestController
@CrossOrigin(origins = ["http://localhost:4200"])
class IndexController {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var authoritiesService: AuthorityService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var log: Logger

    @Autowired
    private lateinit var apiClient: ApiClient

    @Autowired
    private lateinit var modelConverter: ModelConverter

    @Autowired
    lateinit var galleryService: GalleryService

    @GetMapping("/")
    fun index(): String = "Greetings from Spring Boot!"

    @GetMapping("/hello")
    fun test(@RequestParam(value = "name", defaultValue = "World") name: String) = "Hello, $name"

    @GetMapping("/showUsers")
    fun showUsers() = userService.findAll()

    @GetMapping("/me")
    fun showMe(@AuthenticationPrincipal user: User) = user

    @GetMapping("/showAuthorities")
    fun showAuthorities() = authoritiesService.findAll()

    @GetMapping("/encrypt")
    fun encrypt(@RequestParam(value = "name", defaultValue = "World") name: String) =
            passwordEncoder.encode(name)

    @Value("\${spring.datasource.username}")
    lateinit var dbUserName: String

    @Value("\${my.db.name}")
    lateinit var database: String

    @Value("\${postgres.bin.dir}")
    lateinit var postgresBinDir: String

    @Value("\${postgres.pg_dump.arg}")
    lateinit var optionalLocalHostArg: String

    //    @Scheduled(cron = "0 * * * * *") //use it for test every minute
    @Scheduled(fixedDelay = BACK_UP_RATE_MILLIS, initialDelay = BACK_UP_RATE_MILLIS)
    @GetMapping("/createBackUp")
    fun backUpDb(): String {
        log.error("optionalLocalHostArg: $optionalLocalHostArg")
        val timeStampDateFormat = SimpleDateFormat("dd.MM.yyyy_hh.mm.ss")
        val timeStampString = timeStampDateFormat.format(Date())
        //dirty fucking hack motherfucking properties!!!!111one111
        val correctArg = if (optionalLocalHostArg.isEmpty()) optionalLocalHostArg else " $optionalLocalHostArg"
        val dbBackupDirName = "dbBackup"

        val createDirCmd = "mkdir -p $dbBackupDirName"

        var runtimeProcess: Process
        try {
            val pb = ProcessBuilder(createDirCmd.split(" "))
//            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
//            pb.redirectError(ProcessBuilder.Redirect.INHERIT)
            //https://stackoverflow.com/a/40485587/3212712
            val logFile = File("logs/myLogPostgresCreateDir.log")
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            pb.redirectErrorStream(true)
            pb.redirectOutput(logFile)
            runtimeProcess = pb.start()
            val processComplete = runtimeProcess.waitFor()

            if (processComplete == 0) {
                println("Backup dir created successfully")
                log.debug("Backup dir created successfully")
            } else {
                println("Could not create the backup dir")
                log.debug("Could not create the backup dir")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        val executeCmd = "$postgresBinDir/pg_dump -U $dbUserName$correctArg -w -c -f dbBackup/$database" + "_$timeStampString.sql $database"

        log.error("executeCmd: $executeCmd")

        var processComplete: Int
        try {
            val pb = ProcessBuilder(executeCmd.split(" "))
//            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
//            pb.redirectError(ProcessBuilder.Redirect.INHERIT)
            //https://stackoverflow.com/a/40485587/3212712
            val logFile = File("logs/myLogPostgres.log")
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            pb.redirectErrorStream(true)
            pb.redirectOutput(logFile)
            runtimeProcess = pb.start()
            processComplete = runtimeProcess.waitFor()

            if (processComplete == 0) {
                println("Backup created successfully")
                log.debug("Backup created successfully")
            } else {
                println("Could not create the backup")
                log.debug("Could not create the backup")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            processComplete = 1
        }

        return if (processComplete == 0) "{status:\"done\"}" else "{status:\"error\"}"
    }

    @GetMapping("backupList")
    fun backUpFilesList(): String {
        val files = File("").absoluteFile.list(FilenameFilter { dir, name -> name.startsWith(database) })

        return files.joinToString(separator = "</br>")
    }

    @GetMapping("/restoreFromBackUp")
    fun restoreFromBackUp(@RequestParam(value = "fileName") fileName: String): String {
        val formattedFileName = if (fileName.endsWith(".sql")) fileName else "$fileName.sql"
        val executeCmd = "$postgresBinDir/psql --username=$dbUserName --file=$formattedFileName $database"

        val runtimeProcess: Process
        try {
            val pb = ProcessBuilder(executeCmd.split(" "))
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT)
            pb.redirectError(ProcessBuilder.Redirect.INHERIT)
            runtimeProcess = pb.start()
            val processComplete = runtimeProcess.waitFor()

            if (processComplete == 0) {
                println("Backup restored successfully")
            } else {
                println("Could not restore the backup")
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return "{status:\"done\"}"
    }

    @GetMapping("/testVkApiSdk")
    fun testVkApiSdk(): String {
        apiClient.getScpArtPhotosFromVk()?.let {
            val galleryPhotos = modelConverter.convert(it.items)
            galleryService.saveAll(galleryPhotos)
            return "{status:\"done\"}"
        }

        return "{status:\"error\"}"
    }

    @GetMapping("/getGallery")
    fun getGallery() = galleryService.findAll()
}
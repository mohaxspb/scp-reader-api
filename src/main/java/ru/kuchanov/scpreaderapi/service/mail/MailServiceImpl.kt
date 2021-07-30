package ru.kuchanov.scpreaderapi.service.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.model.dto.article.ArticleToLangDto
import ru.kuchanov.scpreaderapi.service.article.ArticleForLangService
import ru.kuchanov.scpreaderapi.service.article.ArticleService
import ru.kuchanov.scpreaderapi.service.article.read.ReadArticleForLangService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Service
class MailServiceImpl @Autowired constructor(
    val javaMailSender: JavaMailSender,
    val userService: ScpReaderUserService,
    val articleService: ArticleService,
    val articleForLangService: ArticleForLangService,
    val readArticlesService: ReadArticleForLangService,
    @Value("\${my.mail.admin.address}") val adminEmailAddress: String
) : MailService {

    override fun getAdminAddress(): String =
        adminEmailAddress

    override fun sendMail(vararg to: String, subj: String, text: String, sendAsHtml: Boolean) {
        javaMailSender.send { mimeMessage: MimeMessage ->
            mimeMessage.setFrom()
            mimeMessage.setRecipients(Message.RecipientType.TO, to.map { InternetAddress(it) }.toTypedArray())
            mimeMessage.subject = subj
            if (sendAsHtml) {
                mimeMessage.setText(text, "utf-8", "html")
            } else {
                mimeMessage.setText(text)
            }
        }
    }

    override fun sendRegistrationEmail(email: String, password: String) {
        val text = """Hello! Welcome to "SCP Reader!"
            |
            | Here is you password, which you can use to login to site. Also you could login using social networks profiles, if you use same email in it. 
            |
            |Your email: $email
            |Your password: $password
        """.trimMargin()
        sendMail(
            email,
            subj = "Welcome to SCP Reader!",
            text = text
        )
    }

    @Scheduled(
        /**
         * second, minute, hour, day, month, day of week
         */
//        cron = "*/30 * * * * *" //fi xme test
        cron = "0 5 0 * * *"
    )
    override fun sendStatisticsEmail() {
        sendStatisticsEmail(today = false)
    }

    override fun sendStatisticsEmail(today: Boolean) {
        val currentDate = LocalDate.now()

        val startDate: Instant
        val endDate: Instant
        if (today) {
            startDate = currentDate.atStartOfDay().toInstant(ZoneOffset.UTC)
            endDate = currentDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        } else {
            startDate = currentDate.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            endDate = currentDate.atStartOfDay().toInstant(ZoneOffset.UTC)
        }

        val dateString = SimpleDateFormat("EEE, dd MMMMM yyyy").format(Date.from(startDate))

        val articlesCreatedToday = articleService.getCreatedArticlesBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
//        println("articlesCreatedToday: $articlesCreatedToday")
        val articlesToLangsCreatedToday: List<ArticleToLangDto> =
            articleForLangService.getCreatedArticleToLangsBetweenDates(
                startDate.toString(),
                endDate.toString()
            )

        val articlesToLangsCountGroupedByLang: Map<String, Int> =
            articlesToLangsCreatedToday
                .groupBy { it.langId }
                .mapValues { it.value.size }

        val createdTranslationsCountByLangAsHtml = articlesToLangsCountGroupedByLang
            .map { "<li>${it.key} = ${it.value}</li>" }
            .joinToString(separator = "\n")

        val usersCreatedTodayCount = userService.countUsersCreatedBetweenDates(
            startDate.toString(),
            endDate.toString()
        )
        val articlesToLangReadCreatedToday = readArticlesService.readArticlesCreatedBetweenDates(
            startDate.toString(),
            endDate.toString()
        )

        val articlesToLangReadCreatedTodayGroupedByLang = articlesToLangReadCreatedToday
            .groupBy { it.langId }
            .mapValues { it.value.size }

        val articlesToLangReadCreatedTodayGroupedByLangAsHtml = articlesToLangReadCreatedTodayGroupedByLang
            .map { "<li>${it.key} = ${it.value}</li>" }
            .joinToString(separator = "\n")

        val text = generateStatisticsText(
            dateString = dateString,
            articlesCreatedTodayCount = articlesCreatedToday.size,
            translationsCreatedTodayCount = articlesToLangsCreatedToday.size,
            createdTranslationsCountByLangAsHtml = createdTranslationsCountByLangAsHtml,
            usersCreatedCount = usersCreatedTodayCount,
            articlesReadCount = articlesToLangReadCreatedToday.size,
            articlesToLangReadCreatedTodayGroupedByLangAsHtml = articlesToLangReadCreatedTodayGroupedByLangAsHtml
        )

        sendMail(
            adminEmailAddress,
            subj = "Statistics for $dateString",
            text = text,
            sendAsHtml = true
        )
    }

    fun generateStatisticsText(
        dateString: String,
        articlesCreatedTodayCount: Int,
        translationsCreatedTodayCount: Int,
        createdTranslationsCountByLangAsHtml: String,
        usersCreatedCount: Int,
        articlesReadCount: Int,
        articlesToLangReadCreatedTodayGroupedByLangAsHtml: String
    ) = """
                |<h1>There is statistics for $dateString</h1> 
                |<h3>Data:</h3>
                |<ol>
                |   <li>Articles added: $articlesCreatedTodayCount</li>
                |   <li>
                |       Translations added: $translationsCreatedTodayCount
                |       <ul>$createdTranslationsCountByLangAsHtml</ul>
                |   </li>
                |</ol>
                |==============================
                |<h3>Activity:</h3>
                |<ol>
                |   <li>Users created: $usersCreatedCount</li>
                |   <li>
                |       Articles read: $articlesReadCount
                |       <ul>$articlesToLangReadCreatedTodayGroupedByLangAsHtml</ul>
                |   </li>
                |</ol>
            """.trimMargin()
}
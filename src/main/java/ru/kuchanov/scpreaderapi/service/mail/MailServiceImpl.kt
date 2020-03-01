package ru.kuchanov.scpreaderapi.service.mail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Service
class MailServiceImpl @Autowired constructor(
        val javaMailSender: JavaMailSender,
        val userService: UserDetailsService,
        @Value("\${my.mail.admin.address}") val adminEmailAddress: String,
        @Value("\${my.site.domain}") val domain: String
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

    override fun sendStatisticsEmail() {
     // todo
    }
}
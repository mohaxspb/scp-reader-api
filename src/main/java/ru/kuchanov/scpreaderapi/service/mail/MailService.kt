package ru.kuchanov.scpreaderapi.service.mail


interface MailService {

    fun getAdminAddress(): String

    fun sendMail(vararg to: String, subj: String, text: String, sendAsHtml: Boolean = false)

    fun sendStatisticsEmail()

    fun sendStatisticsEmail(today: Boolean)

    fun sendRegistrationEmail(email: String, password: String)
}
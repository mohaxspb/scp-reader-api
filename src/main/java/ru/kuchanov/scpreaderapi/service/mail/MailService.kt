package ru.kuchanov.scpreaderapi.service.mail


interface MailService {

    fun sendMail(vararg to: String, subj: String, text: String, sendAsHtml: Boolean = false)

    fun sendStatisticsEmail()

    fun sendRegistrationEmail(email: String, password: String)
}
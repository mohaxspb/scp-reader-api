package ru.kuchanov.scpreaderapi.bean.ads

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "banners")
data class Banner(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "image_url")
        var imageUrl: String? = null,
        @Column(name = "logo_url")
        var logoUrl: String? = null,

        val title: String,
        @Column(name = "sub_title")
        val subTitle: String,
        @Column(name = "cta_button_text")
        val ctaButtonText: String,

        @Column(name = "redirect_url")
        val redirectUrl: String,

        var enabled: Boolean,

        @Column(name = "author_id")
        var authorId: Long? = null,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such banner")
class BannerNotFoundException : RuntimeException()
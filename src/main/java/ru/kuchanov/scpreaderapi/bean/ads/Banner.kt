package ru.kuchanov.scpreaderapi.bean.ads

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "banners")
data class Banner(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @Column(name = "image_url")
        val imageUrl: String,
        @Column(name = "logo_url")
        val logoUrl: String,

        val title: String,
        @Column(name = "sub_title")
        val subTitle: String,
        @Column(name = "cta_button_text")
        val ctaButtonText: String,

        @Column(name = "redirect_url")
        val redirectUrl: String,

        val enabled: Boolean,

        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

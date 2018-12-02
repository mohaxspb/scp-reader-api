package ru.kuchanov.scpreaderapi.bean.gallery

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "gallery_image_translations")
data class GalleryImageTranslation(
        //db
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @ManyToOne(cascade = [CascadeType.REFRESH])
        var galleryImage: GalleryImage? = null,
        //content
        @Column(name = "lang_code")
        val langCode: String,
        @Column(columnDefinition = "TEXT")
        var translation: String,
        //status
        val approved: Boolean = false,
        @Column(name = "author_id")
        var authorId: Long,
        @Column(name = "approver_id")
        val approverId: Long? = null,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)
package ru.kuchanov.scpreaderapi.bean.gallery

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "gallery_image")
data class GalleryImage(
        //db
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Column(name = "vk_id", unique = true)
        val vkId: Long,
        //content
        @Column(name = "image_url")
        val imageUrl: String,
        @OneToMany(
                cascade = [CascadeType.ALL],
                fetch = FetchType.EAGER,
                orphanRemoval = true
        )
        @JoinColumn(name = "galleryImage")
        val galleryImageTranslations: MutableSet<GalleryImageTranslation> = mutableSetOf(),
        //status
        @Column(name = "author_id")
        var authorId: Long,
        val approved: Boolean = false,
        @Column(name = "approver_id")
        val approverId: Long? = null,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || javaClass != other.javaClass) return false
                val that = other as GalleryImage
                return vkId == that.vkId
        }

        override fun hashCode(): Int {
                return Objects.hash(vkId)
        }
}
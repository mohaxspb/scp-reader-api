package ru.kuchanov.scpreaderapi.bean.purchase

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@IdClass(KeyUsersAndroidProducts::class)
@Table(name = "users_android_products")
data class UsersAndroidProducts(
        @Id
        @Column(name = "user_id")
        var userId: Long,
        @Id
        @Column(name = "android_product_id")
        var androidProductId: Long,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyUsersAndroidProducts(
        val userId: Long,
        val androidProductId: Long
) : Serializable
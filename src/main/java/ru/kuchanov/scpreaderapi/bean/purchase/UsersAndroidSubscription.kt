package ru.kuchanov.scpreaderapi.bean.purchase

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*


@Entity
@IdClass(KeyUsersAndroidSubscriptions::class)
@Table(name = "users_android_subscriptions")
data class UsersAndroidSubscription(
        @Id
        @Column(name = "user_id")
        var userId: Long,
        @Id
        @Column(name = "android_subscription_id")
        var androidSubscriptionId: Long,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        @Version
        val updated: Timestamp? = null
)

@NoArgConstructor
data class KeyUsersAndroidSubscriptions(
        val userId: Long,
        val androidSubscriptionId: Long
) : Serializable
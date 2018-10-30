package ru.kuchanov.scpreaderapi.bean.users

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.bean.auth.Authority
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidProduct
import ru.kuchanov.scpreaderapi.bean.purchase.UsersAndroidSubscription
import ru.kuchanov.scpreaderapi.model.user.LeaderboardUser
import ru.kuchanov.scpreaderapi.utils.EncryptionConverter
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "users")
@SqlResultSetMapping(name = "LeaderBoardResult", classes = [
    ConstructorResult(targetClass = LeaderboardUser::class,
            columns = [
                ColumnResult(name = "id", type = Long::class),
                ColumnResult(name = "avatar"),
                ColumnResult(name = "fullName"),
                ColumnResult(name = "score", type = Int::class),
                ColumnResult(name = "levelNum", type = Int::class),
                ColumnResult(name = "scoreToNextLevel", type = Int::class),
                ColumnResult(name = "curLevelScore", type = Int::class),
                ColumnResult(name = "numOfReadArticles", type = Int::class)
            ])
])
data class User(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        //spring security user details
        @Column(name = "username", unique = true)
        var myUsername: String,
        @Convert(converter = EncryptionConverter::class)
        @Column(name = "password")
        var myPassword: String,
        val enabled: Boolean = true,
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "userId", fetch = FetchType.EAGER)
        var userAuthorities: Set<Authority>,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null,
        //firebase
        @Column(name = "full_name")
        var fullName: String? = null,
        @Column(name = "sign_in_reward_gained")
        var signInRewardGained: Boolean? = null,
        var score: Int? = null,
        //level
        @Column(name = "level_num")
        var levelNum: Int? = null,
        @Column(name = "score_to_next_level")
        var scoreToNextLevel: Int? = null,
        @Column(name = "cur_level_score")
        var curLevelScore: Int? = null,
        //social login fields
        @Column(name = "facebook_id")
        var facebookId: String? = null,
        @Column(name = "google_id")
        var googleId: String? = null,
        @Column(name = "vk_id")
        var vkId: String? = null,
        //misc
        @Column(name = "name_first")
        var nameFirst: String? = null,
        @Column(name = "name_second")
        var nameSecond: String? = null,
        @Column(name = "name_third")
        var nameThird: String? = null,
        var avatar: String? = null,
        @Column(name = "main_lang_id")
        var mainLangId: String = ScpReaderConstants.Firebase.FirebaseInstance.EN.lang,
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "userId", fetch = FetchType.EAGER)
        var userAndroidSubscriptions: Set<UsersAndroidSubscription> = setOf(),
        @OneToMany(cascade = [CascadeType.ALL], mappedBy = "userId", fetch = FetchType.EAGER)
        var userAndroidProduct: Set<UsersAndroidProduct> = setOf()
) : UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
            userAuthorities.map { SimpleGrantedAuthority(it.authority) }.toMutableList()

    override fun isEnabled() = enabled

    override fun getUsername() = myUsername

    override fun isCredentialsNonExpired() = true

    override fun getPassword() = myPassword

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true
}

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such user")
class UserNotFoundException : RuntimeException()
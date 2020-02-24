package ru.kuchanov.scpreaderapi.bean.transaction

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "user_data_transactions")
data class UserDataTransaction(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        //content
        @Column(name = "user_id")
        var userId: Long,
        @Column(name = "article_to_lang_id")
        var articleToLangId: Long? = null,
        @Enumerated(EnumType.STRING)
        @Column(name = "transaction_type")
        val transactionType: ScpReaderConstants.UserDataTransactionType,
        @Column(name = "transaction_data", columnDefinition = "TEXT")
        val transactionData: String,
        @Column(name = "score_amount")
        val scoreAmount: Int = 0,
        //dates
        @field:CreationTimestamp
        val created: Timestamp? = null,
        @field:UpdateTimestamp
        val updated: Timestamp? = null
)

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Transaction already exists")
class TransactionAlreadyExistsException : RuntimeException()

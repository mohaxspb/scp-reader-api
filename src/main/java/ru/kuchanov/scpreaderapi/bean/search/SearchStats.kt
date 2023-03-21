package ru.kuchanov.scpreaderapi.bean.search

import javax.persistence.*

@Entity
@Table(name = "search_stats")
data class SearchStats(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "lang_id")
    var langId: String,
    @Column(columnDefinition = "TEXT")
    var query: String,

    @Column(name = "num_of_requests")
    var numOfRequests: Long,
)

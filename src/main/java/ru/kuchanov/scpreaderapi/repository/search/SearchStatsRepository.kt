package ru.kuchanov.scpreaderapi.repository.search

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.scpreaderapi.bean.search.SearchStats

interface SearchStatsRepository : JpaRepository<SearchStats, Long> {

    @Query(
        value = """
            insert into search_stats (lang_id, query, num_of_requests)
            VALUES (:langId, :query, 0)
            ON CONFLICT (lang_id, query) DO UPDATE SET num_of_requests = search_stats.num_of_requests + 1
            RETURNING 0;
            """,
        nativeQuery = true
    )
    fun upsertSearchStats(langId: String, query: String): Long

    @Query(
        value =
        """
            SELECT
            id, lang_id, query, num_of_requests
            FROM search_stats
            ORDER BY num_of_requests DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    fun getMostPopularSearchRequests(limit: Int): List<SearchStats>
}

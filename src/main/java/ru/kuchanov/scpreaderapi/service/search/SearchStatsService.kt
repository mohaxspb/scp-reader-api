package ru.kuchanov.scpreaderapi.service.search

import ru.kuchanov.scpreaderapi.bean.search.SearchStats

interface SearchStatsService {

    fun upsertSearchStats(langId: String, query: String)

    fun getMostPopularSearchRequests(limit: Int): List<SearchStats>
}

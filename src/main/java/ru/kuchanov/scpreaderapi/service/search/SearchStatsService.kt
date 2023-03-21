package ru.kuchanov.scpreaderapi.service.search

interface SearchStatsService {

    fun upsertSearchStats(langId: String, query: String)
}

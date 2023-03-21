package ru.kuchanov.scpreaderapi.service.search

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.repository.search.SearchStatsRepository


@Service
class SearchStatsServiceImpl @Autowired constructor(
    private val searchStatsRepository: SearchStatsRepository,
) : SearchStatsService {

    override fun upsertSearchStats(langId: String, query: String) {
        searchStatsRepository.upsertSearchStats(langId, query)
    }
}

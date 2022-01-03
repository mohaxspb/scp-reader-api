package ru.kuchanov.scpreaderapi.model.user

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import ru.kuchanov.scpreaderapi.utils.FileUtils


data class Level(
    @JsonProperty var id: Int? = null,
    @JsonProperty var score: Int? = null
)

data class LevelsJson(@JsonProperty var levels: List<Level>? = null) {

    /**
     * returns NO_SCORE_TO_MAX_LEVEL if level is already MAX_LEVEL_ID
     */
    fun scoreToNextLevel(userScore: Int, curLevel: Level): Int {
        if (curLevel.id == MAX_LEVEL_ID) {
            return NO_SCORE_TO_MAX_LEVEL
        }
        return getLevelMaxScore(curLevel) - userScore
    }

    private fun getLevelMaxScore(curLevel: Level): Int {
        if (curLevel.id == MAX_LEVEL_ID) {
            return NO_SCORE_TO_MAX_LEVEL
        }
        val nextLevel = levels!![curLevel.id!! + 1]

        val nextLevelScore = nextLevel.score

        return nextLevelScore!!
    }

    fun getLevelForScore(score: Int): Level? = levels!!.findLast { score >= it.score!! }

    companion object {

        private const val FILE_PATH = "data/levels.json"
        const val MAX_LEVEL_ID = 5
        const val NO_SCORE_TO_MAX_LEVEL = -1

        fun getLevelsJson(): LevelsJson = ObjectMapper().readValue(
            FileUtils.getFileAsStringFromResources(FILE_PATH),
            LevelsJson::class.java
        )
    }
}
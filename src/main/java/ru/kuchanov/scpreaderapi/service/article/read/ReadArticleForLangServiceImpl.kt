package ru.kuchanov.scpreaderapi.service.article.read

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.kuchanov.scpreaderapi.ScpReaderConstants
import ru.kuchanov.scpreaderapi.ScpReaderConstants.Score.DEFAULT_SCORE_FOR_READ_ARTICLE
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLang
import ru.kuchanov.scpreaderapi.bean.articles.read.ReadArticleByLangNotFoundException
import ru.kuchanov.scpreaderapi.bean.transaction.UserDataTransaction
import ru.kuchanov.scpreaderapi.bean.users.UserNotFoundException
import ru.kuchanov.scpreaderapi.model.dto.article.AddToReadResultDto
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleProjection
import ru.kuchanov.scpreaderapi.model.dto.article.ReadOrFavoriteArticleToLangDto
import ru.kuchanov.scpreaderapi.repository.article.read.ReadArticlesForLangRepository
import ru.kuchanov.scpreaderapi.repository.article.tags.TagForLangRepository
import ru.kuchanov.scpreaderapi.service.article.image.ArticlesImagesService
import ru.kuchanov.scpreaderapi.service.article.type.ArticleAndArticleTypeService
import ru.kuchanov.scpreaderapi.service.transaction.UserDataTransactionService
import ru.kuchanov.scpreaderapi.service.users.ScpReaderUserService
import javax.transaction.Transactional


@Service
class ReadArticleForLangServiceImpl @Autowired constructor(
        val repository: ReadArticlesForLangRepository,
        val imagesService: ArticlesImagesService,
        val tagsForLangRepository: TagForLangRepository,
        val articleAndArticleTypeService: ArticleAndArticleTypeService,
        val transactionService: UserDataTransactionService,
        val scpReaderUserService: ScpReaderUserService
) : ReadArticleForLangService {

    @Transactional
    override fun deleteById(id: Long) =
            repository.deleteById(id)

    @Transactional
    override fun addArticleToRead(articleToLangId: Long, userId: Long, increaseScore: Boolean): AddToReadResultDto {
        //check DB
        //if null - add transaction and score
        //else - update transaction
        val transaction = transactionService.findByTransactionTypeAndArticleToLangIdAndUserId(
                transactionType = ScpReaderConstants.UserDataTransactionType.READ_ARTICLE,
                articleToLangId = articleToLangId,
                userId = userId
        )

        val userScore: Int

        if (transaction == null) {
            val transactionToSave = UserDataTransaction(
                    articleToLangId = articleToLangId,
                    userId = userId,
                    transactionType = ScpReaderConstants.UserDataTransactionType.READ_ARTICLE,
                    transactionData = true.toString(),
                    scoreAmount = if (increaseScore) DEFAULT_SCORE_FOR_READ_ARTICLE else 0
            )
            transactionService.save(transactionToSave)

            val userInDb = scpReaderUserService.getById(userId) ?: throw UserNotFoundException()
            userScore = scpReaderUserService.save(userInDb.apply { score += transactionToSave.scoreAmount }).score
        } else {
            userScore = scpReaderUserService.getUserScoreById(userId)
            transactionService.save(transaction.copy(transactionData = true.toString()))
        }

        val readArticle = findByArticleToLangIdAndUserId(
                articleToLangId = articleToLangId,
                userId = userId
        ) ?: ReadArticleByLang(articleToLangId = articleToLangId, userId = userId)

        return AddToReadResultDto(
                readArticleByLang = repository.save(readArticle),
                score = userScore
        )
    }

    override fun removeArticleFromRead(articleToLangId: Long, userId: Long): ReadArticleByLang {
        //update transaction if find one
        val transaction = transactionService.findByTransactionTypeAndArticleToLangIdAndUserId(
                transactionType = ScpReaderConstants.UserDataTransactionType.READ_ARTICLE,
                articleToLangId = articleToLangId,
                userId = userId
        )
        if (transaction != null) {
            transactionService.save(transaction.copy(transactionData = false.toString()))
        }

        val alreadySavedReadArticle = findByArticleToLangIdAndUserId(
                articleToLangId = articleToLangId,
                userId = userId
        )
        if (alreadySavedReadArticle != null) {
            deleteById(alreadySavedReadArticle.id!!)
            return alreadySavedReadArticle
        } else {
            throw ReadArticleByLangNotFoundException()
        }
    }

    override fun findByArticleToLangIdAndUserId(articleToLangId: Long, userId: Long): ReadArticleByLang? =
            repository.findByArticleToLangIdAndUserId(articleToLangId, userId)

    override fun findAllByUserIdAndLangId(
            userId: Long,
            langId: String?,
            offset: Int,
            limit: Int
    ): List<ReadOrFavoriteArticleToLangDto> =
            if (langId != null) {
                repository.findAllByUserIdAndLangId(userId, langId, offset, limit)
            } else {
                repository.findAllByUserId(userId, offset, limit)
            }
                    .map { it.toDto().withImages().withTags().withType() }

    override fun readArticlesCreatedBetweenDates(startDate: String, endDate: String): List<ReadOrFavoriteArticleProjection> =
            repository.readArticlesCreatedBetweenDates(startDate, endDate)

    fun ReadOrFavoriteArticleProjection.toDto() =
            ReadOrFavoriteArticleToLangDto(
                    id = id,
                    statusChangedDate = statusChangedDate,
                    articleToLangId = articleToLangId,
                    articleId = articleId,
                    langId = langId,
                    urlRelative = urlRelative,
                    rating = rating,
                    title = title,
                    createdOnSite = createdOnSite,
                    hasIframeTag = hasIframeTag
            )

    fun ReadOrFavoriteArticleToLangDto.withImages(): ReadOrFavoriteArticleToLangDto =
            this.apply { imageUrls = imagesService.findAllByArticleForLangId(articleForLangId = articleToLangId) }

    fun ReadOrFavoriteArticleToLangDto.withTags(): ReadOrFavoriteArticleToLangDto =
            this.apply {
                tagDtos = tagsForLangRepository.getAllForLangIdAndArticleForLangIdAsDto(
                        langId = langId,
                        articleForLangId = articleToLangId
                )
            }

    fun ReadOrFavoriteArticleToLangDto.withType(): ReadOrFavoriteArticleToLangDto =
            this.apply { articleTypeToArticleDto = articleAndArticleTypeService.getByArticleIdAndLangIdAsDto(articleId, langId) }
}

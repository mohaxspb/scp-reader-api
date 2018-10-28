package ru.kuchanov.scpreaderapi.model.facebook

data class ValidatedTokenWrapper(
        val verifiedToken: DebugTokenResponse?,
        val exception: Throwable?
)
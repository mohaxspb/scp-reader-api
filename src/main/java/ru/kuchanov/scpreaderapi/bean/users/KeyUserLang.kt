package ru.kuchanov.scpreaderapi.bean.users

import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable

@NoArgConstructor
data class KeyUserLang(
        val userId: Long,
        val langId: String
) : Serializable
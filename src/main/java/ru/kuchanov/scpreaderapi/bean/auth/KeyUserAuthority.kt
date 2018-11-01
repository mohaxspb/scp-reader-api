package ru.kuchanov.scpreaderapi.bean.auth

import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable

@NoArgConstructor
data class KeyUserAuthority(
        val userId: Long? = null,
        val authority: String? = null
) : Serializable
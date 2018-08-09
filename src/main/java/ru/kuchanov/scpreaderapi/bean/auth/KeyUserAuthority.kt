package ru.kuchanov.scpreaderapi.bean.auth

import ru.kuchanov.scpreaderapi.utils.NoArgConstructor
import java.io.Serializable

@NoArgConstructor
data class KeyUserAuthority(
        val userId: Long,
        val authority: String
) : Serializable
package ru.kuchanov.scpreaderapi.model.facebook

import com.fasterxml.jackson.annotation.JsonProperty

data class DebugTokenResponse(var data: Data? = null) {
    data class Data(
            @JsonProperty("app_id") var appId: Long? = null,
            @JsonProperty("is_valid") var isValid: Boolean? = null
    )
}

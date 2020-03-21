package ru.kuchanov.scpreaderapi.model.firebase

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty


@JsonIgnoreProperties(ignoreUnknown = true)
data class FirebaseAccountKey(
        @JsonProperty("project_id") var projectId: String? = null
)
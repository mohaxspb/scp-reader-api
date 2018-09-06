package ru.kuchanov.scpreaderapi.model.firebase

import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.codehaus.jackson.annotate.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class FirebaseAccountKey(
        @JsonProperty var project_id: String? = null
)
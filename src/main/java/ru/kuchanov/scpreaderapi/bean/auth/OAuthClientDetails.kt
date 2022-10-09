package ru.kuchanov.scpreaderapi.bean.auth

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "oauth_client_details")
data class OAuthClientDetails(
        @Id
        val client_id: String,
        val resource_ids: String,
        val client_secret: String,
        val scope: String,
        val authorized_grant_types: String,
        val web_server_redirect_uri: String,
        val authorities: String,
        val access_token_validity: Int,
        val refresh_token_validity: Int,
        val additional_information: String,
        val autoapprove: String,
        @field:CreationTimestamp
        val created: Timestamp,
        @field:UpdateTimestamp
        val updated: Timestamp
)

class ClientNotFoundError : RuntimeException()
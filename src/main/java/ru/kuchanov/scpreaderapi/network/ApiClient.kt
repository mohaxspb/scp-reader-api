package ru.kuchanov.scpreaderapi.network

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.ServiceActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import org.springframework.beans.factory.annotation.Value
import javax.annotation.PostConstruct

class ApiClient {

    //vk
    @Value("\${my.api.vk.app_id}")
    var vkAppId: Int? = null

    @Value("\${my.api.vk.client_secret}")
    lateinit var vkClientSecret: String

    @Value("\${my.api.vk.service_access_key}")
    lateinit var vkServiceAccessKey: String

    @Value("\${my.api.vk.art_scp.group_id}")
    var artScpVkGroupId: Int? = null

    @Value("\${my.api.vk.art_scp.album_id}")
    var artScpVkAlbumId: Int? = null

    lateinit var transportClient: HttpTransportClient
    lateinit var vk: VkApiClient
    lateinit var actor: ServiceActor

    @PostConstruct
    fun initClassMembers() {
        transportClient = HttpTransportClient()
        vk = VkApiClient(transportClient)
        actor = ServiceActor(vkAppId, vkClientSecret, vkServiceAccessKey)
    }

    fun getVkAccessToken() = vk.oauth()
            .serviceClientCredentialsFlow(vkAppId, vkClientSecret)
            .execute().accessToken

    fun getScpArtPhotosFromVk() = vk.photos().get(actor)
            .ownerId(artScpVkGroupId)
            .albumId(artScpVkAlbumId.toString())
            .execute()
}
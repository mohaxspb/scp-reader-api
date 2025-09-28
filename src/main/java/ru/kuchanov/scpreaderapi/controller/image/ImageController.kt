package ru.kuchanov.scpreaderapi.controller.image

import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.kuchanov.scpreaderapi.ScpReaderConstants


@RestController
@RequestMapping("/" + ScpReaderConstants.Path.IMAGE)
class ImageController @Autowired constructor(
    @Value(value = "\${my.image-proxy}")
    private val imageProxyUrl: String,
) {

    @GetMapping("/provide")
    fun provide(@RequestParam(value = "imageUrl") imageUrl: String): ResponseEntity<InputStreamResource> {
        val proxyImageRequest = "$imageProxyUrl/image?url=$imageUrl"
        val imageResponse = OkHttpClient().newCall(Request.Builder().url(proxyImageRequest).build()).execute()
        val contentType = when {
            imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") -> MediaType.IMAGE_JPEG
            imageUrl.endsWith(".png") -> MediaType.IMAGE_PNG
            imageUrl.endsWith(".gif") -> MediaType.IMAGE_GIF
            imageUrl.contains(".jpg") || imageUrl.contains(".jpeg") -> MediaType.IMAGE_JPEG
            imageUrl.contains(".png") -> MediaType.IMAGE_PNG
            imageUrl.contains(".gif") -> MediaType.IMAGE_GIF
            else -> MediaType.IMAGE_JPEG
        }
        return ResponseEntity.ok()
            .contentType(contentType)
            .body(InputStreamResource(imageResponse.body!!.byteStream()))
    }
}

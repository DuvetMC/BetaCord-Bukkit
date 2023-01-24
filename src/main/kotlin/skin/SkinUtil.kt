package de.olivermakesco.betacord.bukkit.skin

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.Base64

@Serializable
data class MojangUser(val id: String)

@Serializable
data class MojangSkin(val properties: ArrayList<Properties>)

@Serializable
data class Properties(val value: String)

@Serializable
data class MojangSkinDecode(val textures: Textures)

@Serializable
data class Textures(@SerialName("SKIN") val skin: Texture?)

@Serializable
data class Texture(val url: String)

object SkinUtil {
    val cache = hashMapOf<String, String>()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    val MOJANG_API_BASE = "https://api.mojang.com/users/profiles/minecraft/"
    val MOJANG_SESSION_BASE = "https://sessionserver.mojang.com/session/minecraft/profile/"

    suspend fun getHead(username: String): String {
        cache[username] ?: let {
            val id = client.get(MOJANG_API_BASE+username).body<MojangUser>().id
            val toDecode = client.get(MOJANG_SESSION_BASE+id).body<MojangSkin>().properties[0].value
            val decoded = json.decodeFromString<MojangSkinDecode>(Base64.getDecoder().decode(toDecode).decodeToString())
            cache[username] = decoded.textures.skin!!.url.replace("http://textures.minecraft.net/texture/", "https://skin-oldifier.deno.dev/") + "?head"
        }
        return cache[username]!!
    }
}
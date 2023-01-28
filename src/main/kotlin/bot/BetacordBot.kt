package de.olivermakesco.betacord.bot

import de.olivermakesco.betacord.bukkit.BetacordPlugin
import de.olivermakesco.betacord.tryCreate
import de.olivermakesco.betacord.command.Commands
import de.olivermakesco.betacord.skin.SkinUtil
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.behavior.execute
import dev.kord.core.builder.kord.KordBuilder
import dev.kord.core.entity.Webhook
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.Server
import java.io.File

object BetacordBot {
    @Serializable
    data class Config(val token: String, @SerialName("channel") val channelId: Snowflake)

    val config: Config = Json.decodeFromString(
        File("./config/betacord.json").tryCreate("""
            {
                "token": "TOKEN_GOES_HERE",
                "channel": "CHANNEL_ID_HERE"
            }
        """.trimIndent()).readText()
    )

    var webhook: Webhook? = null
    var kord: Kord? = null

    suspend fun init(server: Server) {
        val shutdownHook = Thread {
            runBlocking {
                (kord!!.getChannel(config.channelId)!! as TextChannel).createMessage("**Server stopped.**")
            }
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook)

        Commands.register()
        kord = KordBuilder(config.token).build()
        kord!!.on<MessageCreateEvent> {
            if (message.channelId.value != config.channelId.value) return@on
            if (message.author?.isBot == true) return@on
            val proxy = "<§9${message.getAuthorAsMember()!!.displayName}§r> ${message.content}"
            println(proxy)
            server.broadcastMessage(proxy)
        }
        kord!!.on<ReadyEvent> {
            (kord.getChannel(config.channelId)!! as TextChannel).createMessage("**Server started.**")
        }
        kord!!.login {
            intents += Intent.GuildMessages
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }

    suspend fun sendMessage(user: String, nick: String, content: String) {
        val channel = (kord!!.getChannel(config.channelId)!! as TextChannel)
        if (webhook == null) {
            webhook = channel.webhooks.firstOrNull {
                it.creatorId?.value == kord!!.selfId.value
            } ?: channel.createWebhook("ChatWebhook")
        }
        webhook!!.execute(webhook!!.token!!) {
            avatarUrl = SkinUtil.getHead(user)
            username = "$nick | Minecraft"
            this.content = content
        }
    }

    suspend fun createJoinLeaveMessage(user: String, leave: Boolean) {
        (kord!!.getChannel(config.channelId)!! as TextChannel).createMessage("**$user** ${if (leave) "left" else "joined"} the game.")
    }
}
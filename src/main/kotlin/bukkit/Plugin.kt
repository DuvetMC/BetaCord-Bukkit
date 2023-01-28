@file:OptIn(DelicateCoroutinesApi::class)

package de.olivermakesco.betacord.bukkit

import de.olivermakesco.betacord.command.Commands
import de.olivermakesco.betacord.command.MinecraftCommandContext
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityListener
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

@Serializable
data class Config(val token: String, @SerialName("channel") val channelId: Snowflake)

class BetacordPlugin : JavaPlugin() {
    override fun onDisable() {
        instance = null
    }

    override fun onEnable() {
        GlobalScope.launch {
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

        instance = this
        server.pluginManager.registerEvent(
            Event.Type.PLAYER_CHAT,
            playerEvents,
            Event.Priority.Normal,
            this
        )
        server.pluginManager.registerEvent(
            Event.Type.PLAYER_JOIN,
            playerEvents,
            Event.Priority.Normal,
            this
        )
        server.pluginManager.registerEvent(
            Event.Type.PLAYER_QUIT,
            playerEvents,
            Event.Priority.Normal,
            this
        )


        val shutdownHook = Thread {
            runBlocking {
                (kord!!.getChannel(config.channelId)!! as TextChannel).createMessage("**Server stopped.**")
            }
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    companion object {
        val config: Config = Json.decodeFromString(File("./config/betacord.json").tryCreate("""
            {
                "token": "TOKEN_GOES_HERE",
                "channel": "CHANNEL_ID_HERE"
            }
        """.trimIndent()).readText())
        var kord: Kord? = null
        var instance: BetacordPlugin? = null
        val playerEvents = PlayerEvents()
        var webhook: Webhook? = null
        val nicks: HashMap<String, String> = Json.decodeFromString(File("./config/betacord-nicks.json").tryCreate("{}").readText())
        fun saveNicks() {
            File("./config/betacord-nicks.json").writeText(Json.encodeToString(nicks))
        }
        private fun File.tryCreate(defaultText: String): File {
            parentFile.mkdir()
            if (createNewFile())
                writeText(defaultText)
            return this
        }
    }
}

class PlayerEvents : PlayerListener() {
    suspend fun Kord.sendMessage(user: String, nick: String, content: String) {
        val channel = (getChannel(BetacordPlugin.config.channelId)!! as TextChannel)
        if (BetacordPlugin.webhook == null) {
            BetacordPlugin.webhook = channel.webhooks.firstOrNull {
                it.creatorId?.value == selfId.value
            } ?: channel.createWebhook("ChatWebhook")
        }
        BetacordPlugin.webhook!!.execute(BetacordPlugin.webhook!!.token!!) {
            avatarUrl = SkinUtil.getHead(user)
            username = "$nick | Minecraft"
            this.content = content
        }
    }

    override fun onPlayerChat(event: PlayerChatEvent) {
        event.player.displayName = BetacordPlugin.nicks[event.player.name] ?: event.player.name
        GlobalScope.launch {
            if (event.message.startsWith("p/")) {
                event.isCancelled = true
                Commands.parser.parse(MinecraftCommandContext(event.message.substring(2), event, BetacordPlugin.instance!!.server))
                return@launch
            }
            BetacordPlugin.kord?.sendMessage(event.player.name, event.player.displayName, event.message)
        }
    }

    override fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.displayName = BetacordPlugin.nicks[event.player.name] ?: event.player.name
        GlobalScope.launch {
            BetacordPlugin.kord?.run {
                (getChannel(BetacordPlugin.config.channelId)!! as TextChannel).createMessage("**${event.player.name}** joined the game.")
            }
        }
    }

    override fun onPlayerQuit(event: PlayerQuitEvent) {
        GlobalScope.launch {
            BetacordPlugin.kord?.run {
                (getChannel(BetacordPlugin.config.channelId)!! as TextChannel).createMessage("**${event.player.name}** left the game.")
            }
        }
    }
}
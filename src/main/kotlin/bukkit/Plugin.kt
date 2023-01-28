@file:OptIn(DelicateCoroutinesApi::class)

package de.olivermakesco.betacord.bukkit

import de.olivermakesco.betacord.bot.BetacordBot
import de.olivermakesco.betacord.command.Commands
import de.olivermakesco.betacord.command.MinecraftCommandContext
import de.olivermakesco.betacord.skin.SkinUtil
import de.olivermakesco.betacord.tryCreate
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
import org.bukkit.event.Event
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class BetacordPlugin : JavaPlugin() {
    override fun onDisable() {
    }

    override fun onEnable() {
        instance = this
        GlobalScope.launch {
            BetacordBot.init(server)
        }
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
    }

    companion object {
        var instance: BetacordPlugin? = null
        val playerEvents = PlayerEvents()
        val nicks: HashMap<String, String> = Json.decodeFromString(File("./config/betacord-nicks.json").tryCreate("{}").readText())
        fun saveNicks() {
            File("./config/betacord-nicks.json").writeText(Json.encodeToString(nicks))
        }
    }
}

class PlayerEvents : PlayerListener() {
    override fun onPlayerChat(event: PlayerChatEvent) {
        event.player.displayName = BetacordPlugin.nicks[event.player.name] ?: event.player.name
        GlobalScope.launch {
            if (event.message.startsWith("p/")) {
                event.isCancelled = true
                Commands.parser.parse(MinecraftCommandContext(event.message.substring(2), event, BetacordPlugin.instance!!.server))
                return@launch
            }
            BetacordBot.sendMessage(event.player.name, event.player.displayName, event.message)
        }
    }

    override fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.displayName = BetacordPlugin.nicks[event.player.name] ?: event.player.name
        GlobalScope.launch {
            BetacordBot.createJoinLeaveMessage(event.player.name, false)
        }
    }

    override fun onPlayerQuit(event: PlayerQuitEvent) {
        GlobalScope.launch {
            BetacordBot.createJoinLeaveMessage(event.player.name, true)
        }
    }
}
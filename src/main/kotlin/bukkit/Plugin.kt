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
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.*
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.File

data class LoginState(
    var logged_in: Boolean,
    var login_pos: Location,
    val login: BetacordBot.Login
)

val player_logins = HashMap<String, LoginState>()

var Player.login: LoginState?
    get() {
        return player_logins[name]
    }
    set(value) {
        if (value == null) {
            player_logins.remove(name)
            return
        }
        player_logins[name] = value
    }

class BetacordPlugin : JavaPlugin() {
    override fun onDisable() {
    }

    override fun onEnable() {
        instance = this
        GlobalScope.launch {
            BetacordBot.init(server)
        }

        server.pluginManager.registerEvent(
            Event.Type.PLAYER_MOVE,
            playerEvents,
            Event.Priority.Normal,
            this
        )

        server.pluginManager.registerEvent(
            Event.Type.PLAYER_CHAT,
            playerEvents,
            Event.Priority.Highest,
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

        server.pluginManager.registerEvent(
            Event.Type.PLAYER_INTERACT,
            playerEvents,
            Event.Priority.Normal,
            this
        )

        server.pluginManager.registerEvent(
            Event.Type.PLAYER_INTERACT_ENTITY,
            playerEvents,
            Event.Priority.Normal,
            this
        )

        server.pluginManager.registerEvent(
            Event.Type.PLAYER_COMMAND_PREPROCESS,
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
        val login = event.player.login ?: return

        event.player.displayName = BetacordPlugin.nicks[event.player.name] ?: event.player.name
        val is_command = event.message.startsWith("p/")

        if (is_command)
            event.isCancelled = true
        GlobalScope.launch {
            if (is_command) {
                Commands.parser.parse(MinecraftCommandContext(event.message.substring(2), event, BetacordPlugin.instance!!.server))
                return@launch
            }
            if (!login.logged_in) {
                return@launch
            }
            BetacordBot.sendMessage(event.player.name, event.player.displayName, event.message)
        }
        if (!login.logged_in) {
            event.isCancelled = true
            return
        }
    }

    override fun onPlayerJoin(event: PlayerJoinEvent) {
        val login = BetacordBot.passwords.entries.firstOrNull { it.value.mc_username == event.player.name }?.value

        if (login == null) {
            event.player.kickPlayer("You must register a login with DuvetBot using /login-create")
            return
        }

        event.player.displayName = BetacordPlugin.nicks[event.player.name] ?: event.player.name
        event.player.login = LoginState(false, event.player.location, login)

        event.player.sendMessage("Run 'p/login <passkey>' to login!")

        GlobalScope.launch {
            BetacordBot.createJoinLeaveMessage(event.player.name, false)
        }
    }

    override fun onPlayerQuit(event: PlayerQuitEvent) {
        event.player.login = null
        GlobalScope.launch {
            BetacordBot.createJoinLeaveMessage(event.player.name, true)
        }
    }

    override fun onPlayerMove(event: PlayerMoveEvent) {
        val login = event.player.login ?: return
        if (login.logged_in)
            return
        event.player.teleport(login.login_pos)
    }

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        val login = event.player.login ?: return
        if (login.logged_in)
            return
        event.isCancelled = true
    }

    override fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val login = event.player.login ?: return
        if (login.logged_in)
            return
        event.isCancelled = true
    }

    override fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val login = event.player.login ?: return
        if (login.logged_in)
            return
        event.isCancelled = true
    }

    override fun onPlayerPreLogin(event: PlayerPreLoginEvent) {
        val login = BetacordBot.passwords.entries.firstOrNull { it.value.mc_username == event.name }?.value

        if (login == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_WHITELIST, "You must register a login with DuvetBot using /login-create")
            return
        }
    }
}

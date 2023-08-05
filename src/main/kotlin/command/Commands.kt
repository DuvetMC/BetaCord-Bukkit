package de.olivermakesco.betacord.command

import de.olivermakesco.betacord.bot.BetacordBot
import de.olivermakesco.betacord.bukkit.BetacordPlugin
import de.olivermakesco.betacord.bukkit.login
import dev.proxyfox.command.CommandParser
import dev.proxyfox.command.node.builtin.greedy
import dev.proxyfox.command.node.builtin.literal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.player.PlayerChatEvent

object Commands {
    val parser = CommandParser<PlayerChatEvent, MinecraftCommandContext>()

    suspend fun register() {
        parser.literal("help") {
            executes {
                help(this as MinecraftCommandContext)
                true
            }
        }
        parser.literal("nick") {
            literal("clear") {
                executes {
                    nick_clear(this as MinecraftCommandContext)
                    true
                }
            }
            greedy("name") {
                executes {
                    nick(this as MinecraftCommandContext, it())
                    true
                }
            }
            executes {
                nick(this as MinecraftCommandContext, null)
                true
            }
        }
        parser.literal("login") {
            greedy("passkey") {
                executes {
                    login(this as MinecraftCommandContext, it())
                    true
                }
            }
            executes {
                respondFailure("Incorrect passkey.")
                true
            }
        }
    }

    suspend fun login(
        ctx: MinecraftCommandContext,
        passkey: String
    ) {
        if (ctx.value.player.login?.logged_in == true)
            return
        val login = ctx.value.player.login ?: return
        if (login.login.mc_password != passkey) {
            ctx.respondFailure("Incorrect passkey.")
            return
        }
        login.logged_in = true
        ctx.value.player.teleport(login.login_pos)
        ctx.respondSuccess("Login successful.")
    }

    suspend fun nick_clear(
        ctx: MinecraftCommandContext,
    ) {
        if (ctx.value.player.login?.logged_in != true)
            return
        val old = ctx.value.player.displayName
        ctx.value.player.displayName = ctx.value.player.name
        BetacordPlugin.nicks[ctx.value.player.name] = ctx.value.player.name
        BetacordPlugin.saveNicks()
        ctx.respondSuccess("Nick is now ${ctx.value.player.displayName}", true)
        ctx.respondPlain("$old is now known as ${ctx.value.player.name}")
    }

    suspend fun nick(
        ctx: MinecraftCommandContext,
        nick: String?
    ) {
        if (ctx.value.player.login?.logged_in != true)
            return
        val set = if (nick.isNullOrBlank()) null else nick
        if (set == null) {
            ctx.respondSuccess("Nick is currently ${ctx.value.player.displayName}", true)
            return
        }
        val old = ctx.value.player.displayName
        ctx.value.player.displayName = set
        BetacordPlugin.nicks[ctx.value.player.name] = set
        BetacordPlugin.saveNicks()
        ctx.respondSuccess("Nick is now ${ctx.value.player.displayName}", true)
        ctx.respondPlain("$old is now known as $set")
    }

    suspend fun help(
        ctx: MinecraftCommandContext
    ) {
        ctx.respondSuccess("Help menu for p/", true)
        ctx.respondPlain("  §bp/§ahelp§r - Display help", true)
        ctx.respondPlain("  §bp/§anick §e<nickname>§r - Change your nickname", true)
        ctx.respondPlain("  §bp/§alogin §e<passkey>§r - Login to the server", true)
    }
}

package de.olivermakesco.betacord.command

import de.olivermakesco.betacord.bukkit.BetacordPlugin
import dev.proxyfox.command.node.builtin.greedy
import dev.proxyfox.command.node.builtin.literal
import dev.proxyfox.command.node.builtin.unixLiteral
import org.bukkit.event.player.PlayerChatEvent

object Commands {
    val parser = CommandParser<PlayerChatEvent, MinecraftCommandContext>()

    suspend fun register() {
        parser.literal("help") {
            executes {
                respondSuccess("Help menu for p/", true)
                respondPlain("  §bp/§ahelp§r - Display help", true)
                respondPlain("  §bp/§anick §e<nickname>§r - Change your nickname", true)
                true
            }
        }

        parser.literal("nickname", "nick") {
            executes {
                respondSuccess("Nick is currently ${value.player.displayName}", true)
                true
            }
            unixLiteral("reset", "r") {
                executes {
                    val old = value.player.displayName
                    value.player.displayName = value.player.name
                    BetacordPlugin.nicks[value.player.name] = value.player.name
                    BetacordPlugin.saveNicks()
                    respondSuccess("Nick is now ${value.player.displayName}", true)
                    respondPlain("$old is now known as ${value.player.name}")
                    true
                }
            }
            greedy("nick") {
                executes {
                    val old = value.player.displayName
                    value.player.displayName = it()
                    BetacordPlugin.nicks[value.player.name] = it()
                    BetacordPlugin.saveNicks()
                    respondSuccess("Nick is now ${value.player.displayName}", true)
                    respondPlain("$old is now known as ${it()}")
                    true
                }
            }
        }
    }
}
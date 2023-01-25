package de.olivermakesco.betacord.command

import de.olivermakesco.betacord.bukkit.BetacordPlugin
import dev.proxyfox.command.CommandParser
import dev.proxyfox.command.node.builtin.literal
import dev.proxyfox.command.node.builtin.string
import org.bukkit.event.player.PlayerChatEvent

object Commands {
    val parser = CommandParser<PlayerChatEvent, MinecraftCommandContext>()

    suspend fun register() {
        parser.literal("nickname", "nick") {
            executes {
                respondSuccess("Nick is currently ${value.player.displayName}", true)
                true
            }
            string("nick") {
                executes {
                    value.player.displayName = it()
                    respondSuccess("Nick is now ${value.player.displayName}", true)
                    true
                }
            }
        }
    }
}
package de.olivermakesco.betacord.command

import dev.proxyfox.command.CommandContext
import dev.proxyfox.command.menu.CommandMenu
import org.bukkit.Server
import org.bukkit.event.player.PlayerChatEvent

class MinecraftCommandContext(override val command: String, override val value: PlayerChatEvent, val server: Server) : CommandContext<PlayerChatEvent>() {
    override suspend fun menu(action: suspend CommandMenu.() -> Unit) {

    }

    override suspend fun respondFailure(text: String, private: Boolean): PlayerChatEvent {
        return respondPlain("§c$text", private)
    }

    override suspend fun respondPlain(text: String, private: Boolean): PlayerChatEvent {
        if (private)
            value.player.sendMessage(text)
        else server.broadcastMessage(text)
        return value
    }

    override suspend fun respondSuccess(text: String, private: Boolean): PlayerChatEvent {
        return respondPlain("§a$text", private)
    }

    override suspend fun respondWarning(text: String, private: Boolean): PlayerChatEvent {
        return respondPlain("§e$text", private)
    }

}
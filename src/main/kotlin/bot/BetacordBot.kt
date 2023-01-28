package de.olivermakesco.betacord.bot

import de.olivermakesco.betacord.command.Commands
import de.olivermakesco.betacord.pk
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
import dev.proxyfox.pluralkt.PluralKt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

    val USER_MENTION = Regex("<@([0-9]+)>")
    val ROLE_MENTION = Regex("<@&([0-9]+)>")
    val CHANNEL_MENTION = Regex("<#([0-9]+)>")
    val EMOJI_MENTION = Regex("<(:[a-zA-Z0-9\\-_]+:)([0-9]+)>")

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
            if (message.webhookId != null) return@on
            withContext(Dispatchers.IO) {
                Thread.sleep(100)
            }
            val memberName = message.getAuthorAsMember()!!.displayName
            val processedMessage = processMessage(message.content)+if (message.attachments.size == 1) " §9[Attachment]§r"
            else if (message.attachments.size > 1) " §9[Attachments]§r"
            else ""
            PluralKt.Misc.getMessage(message.id.pk) {
                val name = if (isSuccess()) getSuccess().member?.displayName ?: getSuccess().member?.name ?: memberName else memberName
                val proxy = "<§9$name§r>$processedMessage"
                println(proxy)
                server.broadcastMessage(proxy)
            }
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

    suspend fun processMessage(content: String): String {
        val channel = kord!!.getChannel(config.channelId)!!
        val guild = kord!!.getGuild(channel.data.guildId.value!!)!!
        var output = USER_MENTION.replace(content) {
            runBlocking {
                val member = guild.getMemberOrNull(Snowflake(it.groupValues[1])) ?: return@runBlocking it.value
                "§9@${member.displayName}§r"
            }
        }
        output = ROLE_MENTION.replace(output) {
            runBlocking {
                val role = guild.getRoleOrNull(Snowflake(it.groupValues[1])) ?: return@runBlocking it.value
                "§9@${role.name}§r"
            }
        }
        output = CHANNEL_MENTION.replace(output) {
            runBlocking {
                val channel = guild.getChannelOrNull(Snowflake(it.groupValues[1])) ?: return@runBlocking it.value
                "§9#${channel.name}§r"
            }
        }
        output = EMOJI_MENTION.replace(output) {
            runBlocking {
                "§9${it.groupValues[1]}§r"
            }
        }

        return if (output.isEmpty()) ""
        else " $output"
    }
}
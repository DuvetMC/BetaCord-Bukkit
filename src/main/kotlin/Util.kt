package de.olivermakesco.betacord

import dev.kord.common.entity.Snowflake
import dev.proxyfox.pluralkt.types.PkSnowflake
import java.io.File

fun File.tryCreate(defaultText: String): File {
    parentFile.mkdir()
    if (createNewFile())
        writeText(defaultText)
    return this
}

val Snowflake.pk get() = PkSnowflake(value)
val PkSnowflake.kord get() = Snowflake(value)

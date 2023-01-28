package de.olivermakesco.betacord

import java.io.File

public fun File.tryCreate(defaultText: String): File {
    parentFile.mkdir()
    if (createNewFile())
        writeText(defaultText)
    return this
}
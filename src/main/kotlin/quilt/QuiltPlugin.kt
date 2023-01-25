package de.olivermakesco.betacord.quilt

import org.bukkit.Server
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginLoader
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.config.Configuration
import java.io.File

abstract class QuiltPlugin : JavaPlugin() {
    fun `quilt$initialize`(
        loader: PluginLoader?,
        server: Server?,
        description: PluginDescriptionFile?,
        dataFolder: File?,
        file: File?,
        classLoader: ClassLoader?
    ) {
        super.initialize(loader,server,description, dataFolder, file, classLoader)
    }
}
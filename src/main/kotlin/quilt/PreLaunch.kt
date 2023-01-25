package de.olivermakesco.betacord.quilt

import org.quiltmc.loader.api.ModContainer
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.entrypoint.EntrypointContainer
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint

class PreLaunch : PreLaunchEntrypoint {
    override fun onPreLaunch(mod: ModContainer?) {
        entrypoints = QuiltLoader.getEntrypointContainers("bukkit", QuiltPlugin::class.java)
    }

    companion object {
        lateinit var entrypoints: List<EntrypointContainer<QuiltPlugin>>
    }
}
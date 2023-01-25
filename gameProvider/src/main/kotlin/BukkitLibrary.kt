package de.olivermakesco.betacord.quilt.provider

import net.fabricmc.api.EnvType

enum class BukkitLibrary : Library {
    BUKKIT("org/bukkit/craftbukkit/Main.class");

    private val envType: EnvType?
    private val paths: List<String>

    constructor(envType: EnvType, vararg paths: String) {
        this.envType = envType
        this.paths = listOf(*paths)
    }

    constructor(vararg paths: String) {
        envType = null
        this.paths = listOf(*paths)
    }

    override fun envType(): EnvType? {
        return envType
    }

    override fun paths(): List<String> {
        return paths
    }
}

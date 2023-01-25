package de.olivermakesco.betacord.quilt.provider

import net.fabricmc.api.EnvType

enum class BukkitLibrary : Library {
    BUKKIT("org/bukkit/craftbukkit/Main.class"),
    QUILT_LIBRARIES(
        "org/quiltmc/loader/impl/launch/knot/Knot.class",
        "net/fabricmc/loader/launch/server/FabricServerLauncher.class",
        "org/quiltmc/json5/JsonReader.class",
        "org/quiltmc/config/api/Config.class",
        "org/objectweb/asm/util/ASMifier.class",
        "org/objectweb/asm/tree/ClassNode.class",
        "org/objectweb/asm/commons/JSRInlinerAdapter.class",
        "org/objectweb/asm/tree/analysis/Frame.class",
        "org/objectweb/asm/Opcodes.class",
        "net/fabricmc/accesswidener/AccessWidener.class",
        "org/spongepowered/asm/mixin/Debug.class",
        "net/fabricmc/tinyremapper/AsmRemapper.class",
        "net/fabricmc/mapping/tree/TinyTree.class"
    );

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

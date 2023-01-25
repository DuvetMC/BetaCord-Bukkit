package de.olivermakesco.betacord.quilt.provider

import net.fabricmc.api.EnvType

interface Library {
    fun envType(): EnvType?
    fun paths(): List<String>
    fun path(): String? = paths()[0]
}

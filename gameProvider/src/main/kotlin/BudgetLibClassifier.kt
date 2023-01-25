package de.olivermakesco.betacord.quilt.provider

import net.fabricmc.api.EnvType
import org.quiltmc.loader.impl.util.log.Log
import org.quiltmc.loader.impl.util.log.LogCategory
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.zip.ZipFile


/**
 * @author KJP12
 * @since 2.0.8
 */
class BudgetLibClassifier<T>(private val clazz: Class<T>) where T : Enum<T>, T : Library  {
    private val origins: EnumMap<T, Path> = EnumMap(clazz)
    val unmatched: MutableSet<Path> = HashSet()

    fun process(paths: Collection<Path>, envType: EnvType?) {
        val values: List<T> = if (envType != null) {
            val tmp = ArrayList<T>()
            for (value in clazz.enumConstants) {
                if (value.envType() == null || value.envType() === envType) {
                    tmp.add(value)
                }
            }
            java.util.List.copyOf(tmp)
        } else {
            listOf(*clazz.enumConstants)
        }
        for (path in paths) {
            var found = false
            if (Files.isDirectory(path)) {
                for (lib in values) {
                    for (lpath in lib.paths()) if (Files.exists(path.resolve(lpath))) {
                        found = true
                        val old = origins.putIfAbsent(lib, path)
                        if (old != null) {
                            Log.warn(LogCategory.GENERAL, "Found %s for %s but %s is already present!", path, lib, old)
                        }
                    }
                }
            } else if (Files.isRegularFile(path)) {
                ZipFile(path.toFile()).use { zipfile ->
                    for (lib in values) {
                        for (lpath in lib.paths()) if (zipfile.getEntry(lpath) != null) {
                            found = true
                            val old = origins.putIfAbsent(lib, path)
                            if (old != null) {
                                Log.warn(
                                    LogCategory.GENERAL,
                                    "Found %s for %s but %s is already present!",
                                    path,
                                    lib,
                                    old
                                )
                            }
                        }
                    }
                }
            }
            if (!found) {
                unmatched.add(path)
            }
        }
    }

    fun getOrigin(library: T): Path? {
        return origins[library]
    }

    fun getClassName(library: T): String? {
        if (!origins.containsKey(library)) {
            return null
        }
        val path = library.path()!!
        return if (path.endsWith(".class")) {
            path.substring(0, path.length - 6).replace('/', '.')
        } else null
    }
}
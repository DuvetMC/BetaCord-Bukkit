package de.olivermakesco.betacord.mixin;

import de.olivermakesco.betacord.bukkit.BetacordPlugin;
import de.olivermakesco.betacord.quilt.PreLaunch;
import de.olivermakesco.betacord.quilt.QuiltPlugin;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Pattern;

@Mixin(value = SimplePluginManager.class, remap = false)
public class Mixin_SimplePluginManager {
    @Shadow @Final
    private List<Plugin> plugins;

    @Shadow @Final private Server server;

    @Shadow @Final private Map<Pattern, PluginLoader> fileAssociations;

    @Shadow @Final private Map<String, Plugin> lookupNames;

    @Unique
    private PluginLoader loader = null;

    @Redirect(
            method = "RegisterInterface",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/reflect/Constructor;newInstance([Ljava/lang/Object;)Ljava/lang/Object;",
                    remap = false
            ),
            remap = false
    )
    private <T> T BetaCord$newInstance(Constructor<T> instance, Object[] initargs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var base = instance.newInstance(initargs);
        loader = (PluginLoader) base;
        return base;
    }

    @Inject(
            method = "loadPlugins",
            at = @At("RETURN"),
            remap = false,
            cancellable = true
    )
    private void BetaCord$loadPlugins(File directory, CallbackInfoReturnable<Plugin[]> cir) {
        final ArrayList<Plugin> plugins = new ArrayList<>();
        Collections.addAll(plugins, cir.getReturnValue());
        try {
            System.out.println("Registering quilt plugins!");
            for (var entrypoint : PreLaunch.plugins) {
                var plugin = entrypoint.getEntrypoint();
                System.out.println(entrypoint.getProvider().metadata().name());
                plugin.quilt$initialize(
                        loader,
                        server,
                        new PluginDescriptionFile(
                                entrypoint.getProvider().metadata().name(),
                                entrypoint.getProvider().metadata().version().raw(),

                                entrypoint.getEntrypoint().getClass().getName()
                        ),
                        entrypoint.getProvider().rootPath().toFile(),
                        entrypoint.getProvider().rootPath().toFile(),
                        plugin.getClass().getClassLoader()
                );
                this.lookupNames.put(plugin.getDescription().getName(), plugin);
                this.plugins.add(plugin);
                plugins.add(plugin);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        cir.setReturnValue(plugins.toArray(Plugin[]::new));
    }
}

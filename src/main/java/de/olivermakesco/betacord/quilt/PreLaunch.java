package de.olivermakesco.betacord.quilt;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointContainer;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;

import java.util.List;

public class PreLaunch implements PreLaunchEntrypoint {
    public static List<EntrypointContainer<QuiltPlugin>> plugins;

    @Override
    public void onPreLaunch(ModContainer mod) {
        plugins = QuiltLoader.getEntrypointContainers("bukkit", QuiltPlugin.class);
    }
    static {
        System.out.println(PreLaunch.class.getInterfaces()[0].getClassLoader());
    }
}

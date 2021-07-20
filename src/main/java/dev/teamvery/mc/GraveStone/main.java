package dev.teamvery.mc.GraveStone;

import dev.teamvery.mc.configframework.cfg;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class main extends JavaPlugin {

    public static String plugin_name = "GraveStone";
    public static String items = "items.yml";

    public static FileConfiguration i() {
        return cfg.get(plugin_name, items);
    }

    @Override
    public void onEnable() {
        cfg.makeData(plugin_name, items);

        getServer().getPluginManager().registerEvents(new GraveStoneListener(), this);
    }
}
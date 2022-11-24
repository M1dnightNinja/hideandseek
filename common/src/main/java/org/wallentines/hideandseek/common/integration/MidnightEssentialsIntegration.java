package org.wallentines.hideandseek.common.integration;

import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.util.Util;
import org.wallentines.midnightessentials.api.module.blockcommand.BlockCommandModule;
import org.wallentines.midnightessentials.api.module.blockcommand.BlockCommandRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;

public class MidnightEssentialsIntegration {

    public static void loadBlockCommandsForWorld(Map m, Identifier activeWorld) {

        try {
            File dataFolder = m.getDataFolder();

            FileConfig conf = FileConfig.findFile(dataFolder.listFiles(), "block_commands");
            if (conf == null) return;

            BlockCommandModule mod = MidnightCoreAPI.getModule(BlockCommandModule.class);
            if (mod != null) {

                BlockCommandRegistry reg = mod.createRegistry(activeWorld.toString());
                reg.setActiveWorld(activeWorld);
                reg.loadFromConfig(conf.getRoot());

            }

        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static void unloadBlockCommandsForWorld(Identifier activeWorld) {

        BlockCommandModule mod = MidnightCoreAPI.getModule(BlockCommandModule.class);
        if(mod != null) {

            mod.unloadRegistry(activeWorld.toString());
        }
    }

    public static void saveBlockCommandsForWorld(Map m, Identifier id) {

        File dataFolder = m.getDataFolder();

        BlockCommandModule mod = MidnightCoreAPI.getModule(BlockCommandModule.class);
        if (mod == null) return;

        BlockCommandRegistry reg = mod.getRegistry(id.toString());
        if(reg == null) return;

        FileConfig conf = FileConfig.findOrCreate("block_commands", dataFolder);
        ConfigSection sec = reg.save();

        conf.setRoot(sec);
        conf.save();

    }


}

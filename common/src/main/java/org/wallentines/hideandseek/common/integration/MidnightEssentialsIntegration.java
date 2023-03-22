package org.wallentines.hideandseek.common.integration;

import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.messentials.module.BlockCommandModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;

public class MidnightEssentialsIntegration {

    public static void loadBlockCommandsForWorld(Map m, Identifier activeWorld) {

        try {
            File dataFolder = m.getDataFolder();

            FileWrapper<ConfigObject> conf = FileConfig.find("block_commands", dataFolder);
            if (conf == null) return;

            BlockCommandModule mod = MidnightCoreAPI.getModule(BlockCommandModule.class);
            if (mod != null) {

                BlockCommandModule.Registry reg = mod.createRegistry(activeWorld.toString());
                reg.setActiveWorld(activeWorld);
                reg.loadFromConfig(conf.getRoot().asSection());

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

        BlockCommandModule.Registry reg = mod.getRegistry(id.toString());
        if(reg == null) return;

        FileConfig conf = FileConfig.findOrCreate("block_commands", dataFolder);
        ConfigSection sec = reg.save();

        conf.setRoot(sec);
        conf.save();

    }


}

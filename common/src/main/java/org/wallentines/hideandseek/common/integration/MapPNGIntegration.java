package org.wallentines.hideandseek.common.integration;

import org.wallentines.hideandseek.api.HideAndSeekAPI;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.mappng.MapPNGExtension;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.extension.ServerExtensionModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapPNGIntegration {

    private static final Pattern PATTERN = Pattern.compile("map_(\\d+)\\.png");

    public static void loadMapsForWorld(Map m, Identifier worldId) {

        try {

            ServerExtensionModule mod = MidnightCoreAPI.getModule(ServerExtensionModule.class);
            if (mod == null) {
                return;
            }

            MapPNGExtension ext = mod.getExtension(MapPNGExtension.class);
            if (ext == null) {
                return;
            }

            File dataDir = m.getDataFolder();
            File dir = dataDir.toPath().resolve("maps").toFile();

            if (!dir.isDirectory()) {
                return;
            }

            int loaded = 0;
            File[] files = dir.listFiles();

            if (files != null) for (File f : files) {

                if (!f.isFile()) continue;

                try {
                    Matcher matcher = PATTERN.matcher(f.getName());
                    if (!matcher.matches()) continue;

                    String id = matcher.group(1);
                    int index = Integer.parseInt(id);

                    ext.loadImage(worldId, index, f);
                    loaded++;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            HideAndSeekAPI.getLogger().info("Loaded " + loaded + " PNG maps for map " + m.getId());
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public static int playersWithExtension(Collection<MPlayer> players) {

        ServerExtensionModule mod = MidnightCoreAPI.getModule(ServerExtensionModule.class);
        if(mod == null) return 0;

        MapPNGExtension ext = mod.getExtension(MapPNGExtension.class);
        if(ext == null) return 0;

        int out = 0;
        for(MPlayer mpl : players) {
            if(mod.playerHasExtension(mpl, MapPNGExtension.ID)) out++;
        }

        return out;
    }
}

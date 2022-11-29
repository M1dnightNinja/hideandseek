package org.wallentines.hideandseek.common.integration;

import org.wallentines.hideandseek.api.game.Lobby;
import org.wallentines.hideandseek.api.game.map.Map;
import org.wallentines.hideandseek.common.util.GUIUtil;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

public class IntegrationManager {

    private static boolean MIDNIGHT_ESSENTIALS;
    private static boolean NATIVE_UI;
    private static boolean MAP_PNG;

    public static boolean isMidnightEssentialsPresent() {
        return MIDNIGHT_ESSENTIALS;
    }

    public static boolean isNativeUIPresent() {
        return NATIVE_UI;
    }

    public static boolean isMapPNGPresent() {
        return MAP_PNG;
    }

    public static int playersWithMapPNG(Collection<MPlayer> player) {

        if(MAP_PNG) {
            return MapPNGIntegration.playersWithExtension(player);
        }

        return 0;
    }

    public static void openLobbyMenu(Consumer<Lobby> choice, Function<Lobby, Boolean> filter, boolean showPlayerCount, MPlayer player) {

        if(NATIVE_UI) {
            NativeUIIntegration.openLobbyGUI(choice, filter, showPlayerCount, player);
        } else {
            GUIUtil.lobbyGUI(choice, filter, showPlayerCount, player).open(player, 0);
        }
    }

    public static void openMapMenu(Consumer<Map> choice, Function<Map, Boolean> filter, MPlayer player) {

        if(NATIVE_UI) {
            NativeUIIntegration.openMapGUI(choice, filter, player);
        } else {
            GUIUtil.mapGUI(choice, filter, player).open(player, 0);
        }
    }

    static {

        try {
            Class.forName("org.wallentines.midnightessentials.api.MidnightEssentialsAPI");
            MIDNIGHT_ESSENTIALS = true;
        } catch (ClassNotFoundException ex) {
            // MidnightEssentials not found
        }

        try {
            Class.forName("org.wallentines.nativeui.NativeUIExtension");
            NATIVE_UI = true;
        } catch (ClassNotFoundException ex) {
            // NativeUI not found
        }

        try {
            Class.forName("org.wallentines.mappng.MapPNGExtension");
            MAP_PNG = true;
        } catch (ClassNotFoundException ex) {
            // MapPNG not found
        }

    }

}
